import React, { useState } from "react";
import { useContextUser } from "@/context/UserProvider.tsx";
import { Label } from "@/components/ui/label.tsx";
import { Switch } from "@/components/ui/switch.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { Info } from 'lucide-react';
import { motion } from "framer-motion";
import type { UserSettings } from "@/api/generated/openAPIDefinition.schemas.ts";
import { useUpdateUser } from "@/api/generated/user/user.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { getGetMeQueryKey } from "@/api/generated/authentication/authentication.ts";
import Text from "@/components/typography/Text.tsx";
import { useNavigate, useSearchParams } from "react-router";

type SettingKey = keyof UserSettings;

type settingType = {
    category: string,
    key: SettingKey,
    label: string,
    type: string,
    description?: string
}

const settings: settingType[] = [
    {
        category: "visibility",
        key: "userVisible",
        label: "Show Profile",
        type: "switch",
        description: "Toggle between being visible (on) or invisible (off) to other users. Cannot be set to off when you are owner of a an organization. "
    },
    {
        category: "visibility",
        key: "showAvatar",
        label: "Show Avatar",
        type: "switch",
        description: "Toggle whether your avatar is displayed to others in your profile."
    },
    {
        category: "visibility",
        key: "showOrgas",
        label: "Show Organizations",
        type: "switch",
        description: "Toggle whether your organizations are displayed on your profile."
    },
    {
        category: "visibility",
        key: "showEmail",
        label: "Show Email",
        type: "switch",
        description: "Toggle whether your email is displayed in your profile."
    }
]

const settingCategories = [
    {
        title: "Visibility",
        tag: "visibility",
        description: "Control what information is displayed on your profile."
    },
];

export default function SettingsForm() {

    const { user } = useContextUser();

    const [ searchParams ] = useSearchParams()

    const navigate = useNavigate();

    const fromProfilePage = searchParams.get( "fromProfilePage" ) == "true";

    const updateUser = useUpdateUser( {
        axios: {
            withCredentials: true
        }
    } );
    const queryClient = useQueryClient();

    const initialSettings = { ...user?.userSettings as UserSettings };

    const [ showDescription, setShowDescription ] = useState<Record<string, boolean>>( () =>
        Object.fromEntries( settings.map( setting => [ setting.key, false ] ) )
    );

    const [ currentSettings, setCurrentSettings ] = useState<UserSettings>( initialSettings );

    // shallow compare; might need a better solution for deep objects
    const formIsDirty = JSON.stringify( currentSettings ) === JSON.stringify( initialSettings );

    const handleShowDescription = ( key: string ) => {
        setShowDescription( prevState => ( {
            ...prevState,
            [ key ]: !prevState[ key ]
        } ) );
    }

    const toggleSwitchValue = ( key: string ) => {
        setCurrentSettings( prevState => ( {
            ...prevState,
            [ key ]: !prevState[ key as SettingKey ]
        } ) );
    }

    const handleSubmit = ( event: React.FormEvent<HTMLFormElement> ) => {
        event.preventDefault();

        if ( formIsDirty ) return;

        updateUser.mutate(
            {
                userId: user.id,
                data: {
                    userSettings: currentSettings
                }
            },
            {
                onSuccess: () => {
                    console.log( "User settings updated successfully." );
                    toast.success( "User settings updated successfully." );
                    queryClient.invalidateQueries( { queryKey: getGetMeQueryKey() } );

                    if ( fromProfilePage ) navigate( `/profile/${ user.id }` );
                },
                onError: ( error ) => {
                    console.error( "Failed to update item:" );
                    console.log( error );
                    toast.error( error.response?.data.error || error.message || "Failed to update." );
                }
            }
        );
    }

    const handleCancel = () => {
        if ( formIsDirty ) return;

        setCurrentSettings( initialSettings );
    }

    return (
        <form onSubmit={ handleSubmit }
              className={ "w-11/12 max-w-[600px] flex flex-col items-center justify-center mb-12 gap-8" }>
            { settingCategories.map( ( { title, tag, description } ) => (
                    <>
                        <div key={ title } className={ "w-full space-y-2 pb-1 mb-2" }>
                            <Text asTag={ "h3" }
                                  styleVariant={ "h3" }
                                  className={ "border-b border-primary/60" }>
                                { title }
                            </Text>
                            <Text asTag={ "span" }
                                  styleVariant={ "smallMuted" }>
                                { description }
                            </Text>
                        </div>
                        { settings
                            .filter( ( setting ) => setting.category === tag )
                            .map( ( setting ) => (
                                <div key={ setting.key }
                                     className={ "space-y-2 items-center flex-col flex w-full justify-between" }>
                                    <div
                                        className={ "flex w-full p-2  py-3 rounded-md bg-muted items-center justify-between" }>
                                        <Label htmlFor={ setting.key }>
                                            <Button variant={ "ghost" }
                                                    size={ "icon" }
                                                    className={ "hover:bg-primary" }
                                                    type={ "button" }
                                                    onClick={ () => handleShowDescription( setting.key ) }>
                                                <Info
                                                    className={ `size-4 stroke-accent-foreground ${ showDescription[ setting.key ] && "stroke-primary/80" }` }/>
                                            </Button>
                                            { setting.label }
                                        </Label>
                                        {
                                            setting.type === "switch" &&
                                            <Switch id={ setting.key }
                                                    checked={ currentSettings[ setting.key ] }
                                                    onCheckedChange={ () => toggleSwitchValue( setting.key ) }
                                            />
                                        }
                                    </div>
                                    <motion.div
                                        className={ "w-full text-left px-2 text-sm text-muted-foreground tracking-wide text-pretty" }
                                        initial={ { height: 0, opacity: 0 } }
                                        animate={ showDescription[ setting.key ] ?
                                            { height: "auto", opacity: 1 }
                                            :
                                            { height: 0, opacity: 0 }
                                        }>
                                        { setting.description }
                                    </motion.div>
                                    <Separator/>
                                </div>
                            ) )
                        }
                    </>
                )
            ) }
            <div className={ "w-full p-2 flex justify-between" }>
                <Button variant={ "secondary" } onClick={ handleCancel }>Cancel</Button>
                <Button disabled={ formIsDirty || updateUser.isPending } type={ "submit" }
                        className={ "disabled:cursor-not-allowed disabled:pointer-events-auto" }>Submit</Button>
            </div>
        </form>
    )
}
