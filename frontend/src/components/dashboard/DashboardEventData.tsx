import Text from "@/components/typography/Text.tsx";
import { Progress } from "@/components/ui/progress.tsx";
import type { Event } from "@/api/generated/openAPIDefinition.schemas.ts";

type DashboardEventManagerProps = {
    event: Event
}

export default function DashboardEventData( { event }: DashboardEventManagerProps ) {

    const maxTickets = event.maxTicketCapacity;
    const freeTickets = event.freeTicketCapacity;

    const progressValue = maxTickets ? event.bookedTicketsCount / maxTickets * 100 : 100;

    return (
        <div className={ "w-full grid grid-cols-2 space-y-12 break-words" }>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Id:
            </Text>
            <Text>
                { event.id }
            </Text>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Title:
            </Text>
            <Text className={ "" }>
                { event.title }
            </Text>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Event Start:
            </Text>
            <Text className={ "text-primary" }>
                { new Date( event.eventDateTime ).toLocaleString() }
            </Text>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Tickets booked:
            </Text>
            <Text>
                { event.bookedTicketsCount ?? "Limitless Tickets" }
            </Text>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Max Tickets:
            </Text>
            <Text>
                { event.maxTicketCapacity ?? "Limitless Tickets" }
            </Text>
            <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                Tickets available:
            </Text>
            <Text>
                { event.freeTicketCapacity ?? "Limitless Tickets" }
            </Text>
            {
                maxTickets && freeTickets &&
                <>
                    <Text styleVariant={ "h4" } className={ "text-muted-foreground" }>
                        Progress:
                    </Text>
                    <div className={ "col-span-2 space-y-2" }>
                        <Progress value={ progressValue } className={ "w-full col-span-2" }/>
                        <div className={ "col-span-2 place-items-center" }>
                            <Text>
                                { progressValue + " % booked" }
                            </Text>
                        </div>
                    </div>
                </>
            }
        </div>
    )
}
