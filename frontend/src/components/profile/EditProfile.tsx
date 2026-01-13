import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
    FormMessage
} from "@/components/ui/form.tsx";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import type { AppUser } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Input } from "@/components/ui/input.tsx";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useUpdateUser } from "@/api/generated/user/user.ts";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

type UserFormData = {
    name: string;
    email?: string;
}

const userFormSchema = z.object( {
    name: z.string()
        .min( 1, "Name is required" )
        .min( 3, "Name must be at least 3 characters" )
        .max( 20, "Name must be at most 5 characters" )
        .trim(),
    email: z.email().trim().optional().or( z.literal( `` ) )
} )

type EditProfileProps = {
    userData: AppUser,
    onSubmit: () => void,
}

export default function EditProfile( { userData, onSubmit }: Readonly<EditProfileProps> ) {

    const updateUser = useUpdateUser( {
        axios: {
            withCredentials: true,
        }
    } );

    const queryClient = useQueryClient();

    const form = useForm<UserFormData>( {
        resolver: zodResolver( userFormSchema ),
        defaultValues: {
            name: userData.name,
            email: userData.email || ""
        }
    } )

    const suppressSubmit = !form.formState.isDirty || form.formState.isSubmitting;

    const handleSubmit = ( data: UserFormData ) => {

        if ( suppressSubmit ) {
            console.log( "Form is not dirty or is submitting, returning early." );
            return;
        }

        updateUser.mutate(
            {
                userId: userData.id,
                data: {
                    ...data,
                    userSettings: userData.userSettings
                }
            },
            {
                onSuccess: () => {
                    console.log( "Profile updated successfully." );
                    toast.success( "Profile updated successfully." );
                    queryClient.invalidateQueries();
                    onSubmit();
                },
                onError: ( error ) => {
                    console.error( "Failed to update profile" );
                    toast.error( error.response?.data.error || error.message || "Failed to update." );
                }
            }
        )
    }

    return (
        <Card className={ "p-4 w-full max-w-[600px] mx-auto" }>
            <CardHeader>
                <CardTitle>Edit</CardTitle>
                <CardDescription>
                    Manage your profile information here.
                </CardDescription>
            </CardHeader>
            <CardContent>
                <Form { ...form }>
                    <form onSubmit={ form.handleSubmit( handleSubmit ) } className={ "space-y-6" }>
                        <FormField
                            name={ "name" }
                            control={ form.control }
                            render={
                                ( { field } ) => (
                                    <FormItem>
                                        <FormLabel>
                                            Name:
                                            <FormMessage className={ "text-xs ml-auto leading-0" }/>
                                        </FormLabel>
                                        <FormControl>
                                            <Input placeholder={ "Your Name..." } { ...field }/>
                                        </FormControl>
                                        <FormDescription>
                                            Required
                                        </FormDescription>
                                    </FormItem>
                                )
                            }
                        /> <FormField
                        name={ "email" }
                        control={ form.control }
                        render={
                            ( { field } ) => (
                                <FormItem>
                                    <FormLabel>
                                        Email:
                                        <FormMessage className={ "text-xs ml-auto leading-0" }/>
                                    </FormLabel>
                                    <FormControl>
                                        <Input placeholder={ "Your Email..." } { ...field }/>
                                    </FormControl>
                                    <FormDescription>
                                        Not required
                                    </FormDescription>
                                </FormItem>
                            )
                        }
                    />
                        <Button type={ "submit" } disabled={ suppressSubmit }>
                            Submit
                        </Button>
                    </form>
                </Form>
            </CardContent>
        </Card>
    )
}
