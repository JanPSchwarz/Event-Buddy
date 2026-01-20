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
import type { BookingResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { useDeleteBookingById } from "@/api/generated/booking-controller/booking-controller.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

type DeleteBookingDialogProps = {
    booking: BookingResponseDto
}

export default function DeleteBookingDialog( { booking }: Readonly<DeleteBookingDialogProps> ) {

    const [ open, setOpen ] = useState( false );

    const deleteBooking = useDeleteBookingById( {
        axios: { withCredentials: true }
    } );

    const queryClient = useQueryClient();

    const handleDeleteBooking = () => {
        if ( !booking.bookingId ) {
            toast.error( "BookingId missing." );
            return;
        }

        deleteBooking.mutate( {
            bookingId: booking.bookingId
        }, {
            onSuccess: () => {
                toast.success( "Booking deleted successfully." );
                queryClient.invalidateQueries().then( () => {
                    closeDialog();
                } )
            },
            onError: () => {
                toast.error( "Failed to delete booking." );
            }
        } )
    }

    const closeDialog = () => {
        setOpen( false );
    }

    return (
        <AlertDialog open={ open } onOpenChange={ setOpen }>
            <AlertDialogTrigger asChild>
                <Button variant={ "destructive" }>
                    Delete Booking
                </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
                <div className={ `${ deleteBooking.isPending && "opacity-60" }` }>
                    <AlertDialogHeader>
                        <AlertDialogTitle className={ "mb-4" }>
                            Delete Booking for { booking.hostingEvent.title }
                        </AlertDialogTitle>
                    </AlertDialogHeader>
                    <AlertDialogDescription className={ "space-y-4" }>
                        <Text asTag={ "span" } className={ "block" }>
                            Are you sure you want to delete this Booking?
                        </Text>
                        <Text asTag={ "span" } className={ "block" }>
                            { booking.numberOfTickets } ticket(s) for { booking.name }
                        </Text>
                        <Text asTag={ "span" } className={ "block text-destructive my-2" }>
                            This action cannot be undone.
                        </Text>
                    </AlertDialogDescription>
                    <AlertDialogFooter>
                        <AlertDialogCancel>
                            Cancel
                        </AlertDialogCancel>
                        <ButtonWithLoading variant={ "destructive" }
                                           className={ "relative" }
                                           disabled={ deleteBooking.isPending }
                                           isLoading={ deleteBooking.isPending }
                                           onClick={ handleDeleteBooking }
                        >
                            Delete
                        </ButtonWithLoading>
                    </AlertDialogFooter>
                </div>
            </AlertDialogContent>
        </AlertDialog>
    )
}
