import { Input } from "@/components/ui/input.tsx";
import { Field, FieldLabel } from "@/components/ui/field.tsx";
import { type ChangeEvent, type FormEvent, useState } from "react";
import { useGetAllUsers } from "@/api/generated/user/user.ts";
import Text from "@/components/typography/Text.tsx";
import type { AppUserDto, OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { User } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import {
    getGetOrganizationBySlugQueryKey,
    useAddOwnerToOrganization
} from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

type AddOrganizationOwnerProps = {
    orgData: OrganizationResponseDto
}

export default function AddOrganizationOwner( { orgData }: Readonly<AddOrganizationOwnerProps> ) {

    const [ searchQuery, setSearchQuery ] = useState( "" );

    const [ foundUsers, setFoundUsers ] = useState<AppUserDto[]>( [] );

    const { data: allUsers } = useGetAllUsers();

    const queryClient = useQueryClient();
    const addOwner = useAddOwnerToOrganization(
        {
            axios: {
                withCredentials: true,
            }
        }
    );

    const arrayOfCurrentOwnerIds = orgData.owners?.map( ( owner ) => owner.id );

    const handleSubmit = ( event: FormEvent ) => {
        event.preventDefault();
    }

    const handleInputChange = ( event: ChangeEvent<HTMLInputElement> ) => {
        setSearchQuery( event.target.value );

        if ( event.target.value.length <= 1 ) {
            setFoundUsers( [] );
            return;
        }

        if ( allUsers?.data && allUsers.data.length > 0 ) {
            const filteredUsers = allUsers.data.filter( ( user ) =>
                user.name.toLowerCase().includes( searchQuery.toLowerCase() )
            );

            const filteredUsersExcludingOwners = filteredUsers.filter( ( user ) =>
                !arrayOfCurrentOwnerIds?.includes( user.id )
            );

            setFoundUsers( filteredUsersExcludingOwners );
        }
    }

    const handleAddUser = ( userId: string | undefined ) => {
        if ( userId ) {
            addOwner.mutate(
                {
                    organizationId: orgData.id || "",
                    userId: userId
                },
                {
                    onSuccess: () => {
                        toast.success( "Owner added successfully." );
                        queryClient.invalidateQueries( { queryKey: getGetOrganizationBySlugQueryKey( orgData.slug ) } ).then( () => {
                            setSearchQuery( "" );
                            setFoundUsers( [] );
                        
                        } );
                    },
                    onError: ( error ) => {
                        toast.error( error.response?.data.error || error.message || "Failed to add user." );
                    }
                }
            );
        }

    }

    return (
        <div
            className={ `my-8 flex flex-col gap-8` }>
            <form onSubmit={ handleSubmit }>
                <Field>
                    <FieldLabel className={ "flex justify-between" }>
                        Search User to Add as Owner
                        <Text styleVariant={ "smallMuted" } className={ "text-xs" }>
                            Only visible users can be found
                        </Text>
                    </FieldLabel>
                    <Input onChange={ handleInputChange }
                           value={ searchQuery }
                           placeholder={ "Search user by name..." }/>
                </Field>
            </form>
            <div className={ `overflow-y-scroll ${ foundUsers.length > 0 ? "h-[300px]" : "h-auto" }` }>
                { foundUsers.length > 0 &&
                    <Text styleVariant={ "smallMuted" } className={ "text-xs mb-2" }>
                        { foundUsers.length } user(s) found.
                    </Text>
                }
                {
                    foundUsers.length === 0 && searchQuery.length > 1 &&
                    <Text styleVariant={ "smallMuted" } className={ "" }>
                        No users found matching "{ searchQuery }".
                    </Text>
                }
                <ul className={ `flex flex-col gap-4` }>
                    {
                        foundUsers.length > 0 &&
                        foundUsers.map( ( user ) => {
                                return (
                                    <li key={ user.id }
                                        className={ "rounded flex items-center justify-between" }>
                                        <div className={ "flex items-center gap-4" }>
                                            <Avatar>
                                                <AvatarImage src={ user.avatarUrl }/>
                                                <AvatarFallback>
                                                    <User/>
                                                </AvatarFallback>
                                            </Avatar>
                                            <p>{ user.name }</p>
                                        </div>
                                        <Button onClick={ () => handleAddUser( user.id ) } variant={ "outline" }>
                                            Add
                                        </Button>
                                    </li>
                                )
                            }
                        )
                    }
                </ul>
            </div>
        </div>
    )
}
