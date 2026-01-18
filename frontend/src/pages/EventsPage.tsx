import { useGetAllEvents } from "@/api/generated/event-controller/event-controller.ts";
import CustomLoader from "@/components/CustomLoader.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import { Plus, TicketIcon } from "lucide-react";
import EventCard from "@/components/event/EventCard.tsx";
import { Button } from "@/components/ui/button.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";
import { useNavigate } from "react-router";

export default function EventsPage() {

    const { data: allEvents, isLoading } = useGetAllEvents();

    const { user } = useContextUser()

    const navigate = useNavigate();

    if ( isLoading ) {
        return (
            <CustomLoader size={ "size-6" } text={ "Loading..." }/>
        );
    }

    const handleCreateEvent = () => {
        if ( !user?.id ) {
            toast.error( "You must be logged in!" );
        } else {
            navigate( "/event/create" );
        }
    }

    return (
        <PageWrapper className={ "space-y-12 mb-12" }>
            <div className={ "w-full space-y-4 flex flex-col" }>
                <div className={ "w-full flex justify-between" }>
                    <MainHeading
                        heading={ "Events" }
                        subheading={ "Find or create a new Event" }
                        Icon={ TicketIcon }
                        iconClassNames={ "rotate-45" }
                    />
                </div>
                <Button size={ "sm" } className={ "ml-auto" } onClick={ handleCreateEvent }>
                    <Plus/>
                    New Event
                </Button>
            </div>
            <div className={ "flex items-start justify-center flex-wrap gap-12 w-full" }>
                {
                    allEvents?.data.map( ( event ) => {
                        return <EventCard key={ event.id } event={ event }/>
                    } )
                }
                {
                    ( allEvents?.data.length === 0 || !allEvents?.data ) &&
                    <p>No events found.</p>
                }
            </div>
        </PageWrapper>
    )
}
