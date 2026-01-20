import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";
import BookingCard from "@/components/booking/BookingCard.tsx";
import type { BookingResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import { Search } from 'lucide-react';

type DashboardBookingsProps = {
    bookings: BookingResponseDto[] | undefined,
    isLoading?: boolean,

}

export default function DashboardBookings( { bookings = [], isLoading }: Readonly<DashboardBookingsProps> ) {

    return (
        <div className={ "space-y-4 mb-12" }>
            <div className={ "w-full flex" }>
                <Button size={ "sm" } className={ "ml-auto" } asChild>
                    <NavLink to={ "/events" }>
                        <Search/> Find an Event
                    </NavLink>
                </Button>
            </div>
            {
                isLoading &&
                <CustomLoader/>
            }
            {
                !isLoading && bookings.length === 0 ?
                    <Text className={ "text-center" }>
                        No Bookings to display.
                    </Text>
                    :
                    <div className={ "flex gap-8 justify-center md:justify-start flex-wrap" }>
                        { bookings?.map( ( booking ) => (
                            <BookingCard booking={ booking } key={ booking.bookingId }/>
                        ) ) }
                    </div>
            }
        </div>
    )
}
