import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import type { BookingResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import EventImage from "@/components/event/EventImage.tsx";
import Text from "@/components/typography/Text.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";

type BookingCardProps = {
    booking: BookingResponseDto
}

export default function BookingCard( { booking }: Readonly<BookingCardProps> ) {

    const { data: imageData } = useGetImageAsDataUrl( booking.hostingEvent.imageId || "", {
        query: { enabled: !!booking.hostingEvent.imageId },
    } )

    return (
        <Card>
            <CardHeader>
                <CardDescription>
                    Booking for
                </CardDescription>
                <CardTitle>
                    { booking.hostingEvent.title }
                </CardTitle>
                <Text styleVariant={ "smallMuted" }>
                    { new Date( booking.hostingEvent.eventDateTime ).toLocaleDateString( "en-US" ) }, { new Date( booking.hostingEvent.eventDateTime ).toLocaleTimeString( "en-US", {
                    hour: "2-digit",
                    minute: "2-digit"
                } ) }
                </Text>
            </CardHeader>
            <CardContent className={ "w-[250px] space-y-4" }>
                <EventImage imageData={ imageData?.data || null }/>
                <Text styleVariant={ "smallMuted" }>
                    booking Id: { booking.bookingId }
                </Text>
                <Text className={ "text-sm md:text-sm" }>
                    { booking.numberOfTickets } ticket(s) booked for { booking.name }
                </Text>
            </CardContent>
        </Card>
    )
}
