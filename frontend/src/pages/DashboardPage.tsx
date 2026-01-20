import PageWrapper from "@/components/shared/PageWrapper.tsx";
import { useParams } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";
import { useGetUserById } from "@/api/generated/user/user.ts";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { useGetEventsByUserId } from "@/api/generated/event-controller/event-controller.ts";
import { useGetBookingsByUser } from "@/api/generated/booking-controller/booking-controller.ts";
import DashboardBookings from "@/components/dashboard/DashboardBookings.tsx";
import DashboardEvents from "@/components/dashboard/DashboardEvents.tsx";
import DashboardOrgas from "@/components/dashboard/DashboardOrgas.tsx";
import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion.tsx";


export default function DashboardPage() {

    const { userId } = useParams();

    const { user } = useContextUser();

    const { data: bookings, isLoading: bookingsLoading } = useGetBookingsByUser( user.id || "", {
            axios: { withCredentials: true },
            query: {
                enabled: !!user.id
            }
        }
    );

    const { data: userDto, isLoading: organizationsLoading } = useGetUserById( user.id, {
        query: {
            enabled: !!userId
        }
    } );

    const { data: eventsByUser, isLoading: eventsLoading } = useGetEventsByUserId( user.id, {
        query: {
            enabled: !!userId
        }
    } );

    return (
        <PageWrapper className={ "mb-12" }>
            <MainHeading heading={ "Dashboard" } subheading={ user.name }/>
            <div className={ "w-11/12 space-y-12" }>
                <Accordion type={ "multiple" } defaultValue={ [ "bookings", "organizations", "events" ] }
                           className={ "w-full space-y-12" }>
                    <AccordionItem value={ "bookings" }>
                        <AccordionTrigger className={ "bg-muted px-4 mb-4" }>
                            Bookings
                        </AccordionTrigger>
                        <AccordionContent>
                            <DashboardBookings bookings={ bookings?.data } isLoading={ bookingsLoading }/>
                        </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value={ "organizations" }>
                        <AccordionTrigger className={ "bg-muted px-4 mb-4" }>
                            Organizations
                        </AccordionTrigger>
                        <AccordionContent>
                            <DashboardOrgas organizations={ userDto?.data.organizations as OrganizationResponseDto[] }
                                            isLoading={ organizationsLoading }/>
                        </AccordionContent>
                    </AccordionItem>
                    <AccordionItem value={ "events" }>
                        <AccordionTrigger className={ "bg-muted px-4 mb-4" }>
                            Events
                        </AccordionTrigger>
                        <AccordionContent>
                            <DashboardEvents isLoading={ eventsLoading } events={ eventsByUser?.data }/>
                        </AccordionContent>
                    </AccordionItem>

                </Accordion>
            </div>
        </PageWrapper>
    )
}
