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
import Text from "@/components/typography/Text.tsx";
import ButtonWithLoading from "@/components/shared/ButtonWithLoading.tsx";
import { useNavigate, useParams } from "react-router";
import { useDeleteEventById } from "@/api/generated/event-controller/event-controller.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { useContextUser } from "@/context/UserProvider.tsx";

export default function DeleteEventDialog() {

    const { eventId } = useParams();

    const deleteEvent = useDeleteEventById( {
        axios: { withCredentials: true },
    } )

    const queryClient = useQueryClient();

    const { user } = useContextUser();

    const navigate = useNavigate();

    const handleDeleteEvent = () => {
        if ( !eventId ) {
            toast.error( "Event ID is missing" );
            return;
        }

        deleteEvent.mutate(
            { eventId },
            {
                onSuccess: () => {
                    queryClient.invalidateQueries( { refetchType: "none" } ).then( () => {
                        toast.success( "Event deleted successfully" );
                        navigate( `/dashboard/${ user.id }` );
                    } )
                },
                onError: ( error ) => {
                    toast.error( error.response?.data.error || "Error deleting Event" );
                },
            }
        )
    }

    return (
        <AlertDialog>
            <AlertDialogTrigger asChild>
                <Button variant={ "destructive" }>
                    Delete Event
                </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
                <div className={ `${ deleteEvent.isPending && "opacity-60" }` }>
                    <AlertDialogHeader>
                        <AlertDialogTitle>
                            Delete
                        </AlertDialogTitle>
                    </AlertDialogHeader>
                    <AlertDialogDescription>
                        Are you sure you want to delete this Event?
                        <Text asTag={ "span" } className={ "block text-destructive my-2" }>
                            All Bookings will be deleted, too. This action cannot be undone.
                        </Text>
                    </AlertDialogDescription>
                    <AlertDialogFooter>
                        <AlertDialogCancel>
                            Cancel
                        </AlertDialogCancel>
                        <ButtonWithLoading variant={ "destructive" }
                                           className={ "relative" }
                                           onClick={ handleDeleteEvent }
                                           disabled={ deleteEvent.isPending }
                                           isLoading={ deleteEvent.isPending }
                        >
                            Delete
                        </ButtonWithLoading>
                    </AlertDialogFooter>
                </div>
            </AlertDialogContent>
        </AlertDialog>
    )
}
