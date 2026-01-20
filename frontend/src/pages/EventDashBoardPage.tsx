import { NavLink, useParams } from "react-router";
import { useGetRawEventById } from "@/api/generated/event-controller/event-controller.ts";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { ArrowLeftIcon } from "lucide-react";
import { Progress } from "@/components/ui/progress.tsx";
import Text from "@/components/typography/Text.tsx";

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

    const maxTickets = eventData?.data?.maxTicketCapacity;
    const freeTickets = eventData?.data?.freeTicketCapacity;

    const progressValue = maxTickets ? eventData.data.bookedTicketsCount / maxTickets * 100 : 100;

    return (
        <PageWrapper className={ "w-11/12 mx-auto max-w-[1000px]" }>
            <MainHeading heading={ "Event Manager" } subheading={ eventData.data.title || "" }/>
            <div className={ "w-full flex justify-between" }>
                <Button asChild variant={ "outline" }>
                    <NavLink to={ `/dashboard/${ user.id }` }>
                        <ArrowLeftIcon/>
                        Go to dashboard
                    </NavLink>
                </Button>
                <div className={ "space-x-4" }>
                    <Button disabled>
                        Download Guest List
                    </Button>
                    <Button asChild variant={ "outline" }>
                        <NavLink to={ `/event/edit/${ eventData.data.id }?src=manager` }>
                            Edit Event
                        </NavLink>
                    </Button>
                </div>
            </div>
            <div className={ "w-full space-y-4" }>
                <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                    Id:
                </Text>
                <Text>
                    { eventData.data.id }
                </Text>
                <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                    Title:
                </Text>
                <Text className={ "" }>
                    { eventData?.data.title }
                </Text>
                <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                    Event Start:
                </Text>
                <Text className={ "text-primary" }>
                    { new Date( eventData.data.eventDateTime ).toLocaleString() }
                </Text>
                <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                    Tickets booked:
                </Text>
                <Text>
                    { eventData.data.bookedTicketsCount ?? "Limitless Tickets" }
                </Text>
                <Progress value={ progressValue } className={ "w-full" }/>
                <div className={ "flex justify-between" }>
                    <Text>
                        Max Tickets: { maxTickets ?? "no limit" }
                    </Text>
                    <Text>
                        { maxTickets && progressValue + " % booked" }
                    </Text>
                    <Text>
                        Tickets Available: { freeTickets ?? "-" }
                    </Text>
                </div>
            </div>
        </PageWrapper>
    )
}
