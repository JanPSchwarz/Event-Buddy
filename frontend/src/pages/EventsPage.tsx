import { useGetAllEvents } from "@/api/generated/event-controller/event-controller.ts";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import { Plus, TicketIcon } from "lucide-react";
import EventCard from "@/components/event/EventCard.tsx";
import { Button } from "@/components/ui/button.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";
import { useNavigate, useSearchParams } from "react-router";

export default function EventsPage() {

    const { data: allEvents, isPending } = useGetAllEvents();

    const { user } = useContextUser();

    const [ _, setSearchParams ] = useSearchParams();

    const navigate = useNavigate();

    if ( isPending ) {
        return (
            <CustomLoader size={ "size-6" } text={ "Loading..." }/>
        );
    }

    const handleCreateEvent = () => {
        if ( Object.keys( user ).length > 0 ) {
            navigate( "/event/create" );
        } else {
            setSearchParams( ( searchParams ) => {
                searchParams.set( "loginModal", "true" );
                return searchParams;
            } )
            toast.error( "You must be logged in!" );
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
