import { NavLink, useParams } from "react-router";
import { useGetRawEventById } from "@/api/generated/event-controller/event-controller.ts";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { ArrowLeftIcon } from "lucide-react";
import DashboardEventData from "@/components/dashboard/DashboardEventData.tsx";
import DeleteEventDialog from "@/components/dashboard/DeleteEventDialog.tsx";

export default function EventDashBoardPage() {

    const { eventId } = useParams();

    const { user } = useContextUser();

    const { data: eventData, isLoading } = useGetRawEventById( eventId || "", {
        axios: { withCredentials: true },
        query: {
            enabled: !!eventId
        }
    } );


    if ( isLoading ) {
        return (
            <CustomLoader/>
        )
    }

    if ( !eventData?.data ) return null;

    return (
        <PageWrapper className={ "w-11/12 mx-auto max-w-[1000px] mb-12" }>
            <MainHeading heading={ "Event Manager" } subheading={ eventData.data.title }/>
            <div
                className={ "w-full mb-6 flex items-start md:flex-row flex-col gap-8" }>
                <Button asChild variant={ "outline" }>
                    <NavLink to={ `/dashboard/${ user.id }` }>
                        <ArrowLeftIcon/>
                        Go to Dashboard
                    </NavLink>
                </Button>
                <div className={ "flex items-end md:items-end md:justify-end flex-wrap w-full gap-4" }>
                    <Button disabled>
                        Download Guest List
                    </Button>
                    <Button asChild variant={ "outline" }>
                        <NavLink to={ `/event/edit/${ eventData.data.id }?src=manager` }>
                            Edit Event
                        </NavLink>
                    </Button>
                    <DeleteEventDialog/>
                </div>
            </div>
            <DashboardEventData event={ eventData.data }/>
        </PageWrapper>
    )
}
