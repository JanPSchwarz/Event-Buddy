import type { EventResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Badge } from "@/components/ui/badge.tsx";
import { twMerge } from "tailwind-merge";

type EventBadgesProps = {
    event: EventResponseDto,
    badgeClassNames?: string,
    wrapperClassNames?: string,
}

export default function EventBadges( { event, badgeClassNames, wrapperClassNames }: Readonly<EventBadgesProps> ) {

    const hasBadges = event.isSoldOut || event.price === 0 || event.ticketAlarm;

    if ( !hasBadges ) return null;

    return (
        <div className={ twMerge( "space-x-1", wrapperClassNames ) }>
            { event.isSoldOut &&
                <Badge variant={ "destructive" } className={ badgeClassNames }>
                    Sold Out
                </Badge> }
            { event.ticketAlarm &&
                <Badge className={ badgeClassNames }>
                    Almost Sold Out
                </Badge> }
            {
                event.price === 0 &&
                <Badge className={ badgeClassNames }>
                    Free
                </Badge>
            }
        </div>
    )
}
