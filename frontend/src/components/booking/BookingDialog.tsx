import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTrigger } from "@/components/ui/dialog.tsx";
import { Button } from "@/components/ui/button.tsx";
import { DialogTitle } from "@radix-ui/react-dialog";
import BookingForm from "@/components/booking/BookingForm.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import type { EventResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import Text from "@/components/typography/Text.tsx";
import { useState } from "react";
import BookingSuccess from "@/components/booking/BookingSuccess.tsx";

type BookingDialogProps = {
    event: EventResponseDto
}

export default function BookingDialog( { event }: Readonly<BookingDialogProps> ) {

    const { user } = useContextUser();

    const [ bookingCompleted, setBookingCompleted ] = useState( false );

    const handleBookingCompleted = () => {
        setBookingCompleted( true );
    }

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Button>
                    Book Now
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Booking</DialogTitle>
                    { !bookingCompleted ?
                        <DialogDescription>
                            Please fill in the form below to complete your booking.
                            {
                                event.maxPerBooking && !event.isSoldOut &&
                                <Text asTag={ "span" } className={ "mt-2 block text-primary" }>
                                    Max. { event.maxPerBooking } ticket(s) per booking
                                </Text>
                            }
                            {
                                !user?.name &&
                                <Text asTag={ "span" } className={ "mt-2 block text-red-600" }>
                                    You need to be logged in to book tickets.
                                </Text>
                            }
                        </DialogDescription>
                        :
                        <DialogDescription>
                            Your booking was successful!
                        </DialogDescription>
                    }
                </DialogHeader>
                {
                    bookingCompleted ?
                        <BookingSuccess event={ event }/>
                        :
                        <BookingForm user={ user } price={ event?.price } isSoldOut={ event.isSoldOut }
                                     eventId={ event.id }
                                     bookingCompleted={ handleBookingCompleted }/>
                }
            </DialogContent>
        </Dialog>
    )
}
