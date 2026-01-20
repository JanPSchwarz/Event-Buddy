import {
    AlertDialog,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger
} from "@/components/ui/alert-dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useQueryClient } from "@tanstack/react-query";
import { useDeleteOrganization } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useNavigate } from "react-router";
import Text from "@/components/typography/Text.tsx";
import ButtonWithLoading from "@/components/shared/ButtonWithLoading.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";

type DeleteOrganizationDialogProps = {
    organizationId: string,
    isEditing: boolean,
    orgaName: string,
}

export default function DeleteOrganizationDialog( {
                                                      organizationId,
                                                      isEditing,
                                                      orgaName
                                                  }: Readonly<DeleteOrganizationDialogProps> ) {

    const navigate = useNavigate();

    const { user } = useContextUser();

    const queryClient = useQueryClient();

    const deleteOrga = useDeleteOrganization( {
        axios: { withCredentials: true }
    } );
    const handleDeleteOrganization = () => {
        if ( isEditing ) return;


        deleteOrga.mutate(
            { organizationId },
            {
                onSuccess: () => {
                    toast.success( "Organization deleted successfully." );
                    queryClient.invalidateQueries().then( () => {
                        navigate( `/dashboard/${ user.id }` );
                    } )

                },
                onError: ( error ) => {
                    console.error( "Error deleting organization:", error );
                    toast.error( error.response?.data.error || "Error deleting orga" );
                }
            }
        )
    }


    return (
        <AlertDialog>
            <AlertDialogTrigger asChild>
                <Button disabled={ isEditing } variant={ "destructive" }>
                    Delete Organization
                </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>
                        Delete { orgaName }
                    </AlertDialogTitle>
                </AlertDialogHeader>
                <AlertDialogDescription>
                    Are you sure you want to delete this organization?
                    <Text asTag={ "span" } className={ "block text-destructive my-2" }>
                        All Events and Bookings will be deleted, too. This action cannot be undone.
                    </Text>
                </AlertDialogDescription>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        Cancel
                    </AlertDialogCancel>
                    <ButtonWithLoading variant={ "destructive" } isLoading={ deleteOrga.isPending }
                                       disabled={ deleteOrga.isPending }
                                       onClick={ handleDeleteOrganization } className={ "relative" }>
                        Delete { orgaName }
                    </ButtonWithLoading>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    )
}
