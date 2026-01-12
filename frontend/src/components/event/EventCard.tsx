import type { EventResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Card, CardAction, CardContent, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import EventImage from "@/components/event/EventImage.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { Button } from "@/components/ui/button.tsx";
import EventCalendarSheet from "@/components/event/EventCalendarSheet.tsx";
import Text from "@/components/typography/Text.tsx";
import EventBadges from "@/components/event/EventBadges.tsx";
import { NavLink } from "react-router";

type EventCardProps = {
    event: EventResponseDto
}

export default function EventCard( { event }: Readonly<EventCardProps> ) {


    const { data: imageData, isLoading } = useGetImageAsDataUrl( event?.imageId || "", {
        query: { enabled: !!event?.imageId },
    } )

    return (
        <div className={ "max-w-min" }>
            <div className={ " px-2 flex justify-between" }>
                <Text className={ "text-muted-foreground" }>
                    { event.location?.city }
                </Text>
                <Text asTag={ "span" } styleVariant={ "smallMuted" }>
                    { new Date( event.eventDateTime ).getFullYear() }
                </Text>
            </div>
            <Card className={ "w-[300px] space-y-0 gap-3 pt-0 pb-2" }>
                <CardContent className={ "space-y-4 p-0" }>
                    <EventImage imageData={ imageData?.data }
                                imageClassName={ "rounded-none rounded-t-md" }
                                isLoading={ isLoading }/>
                </CardContent>
                <CardHeader className={ "gap-1 px-4" }>
                    <div className={ "flex justify-between items-start" }>
                        <div className={ "px-1 space-y-2" }>
                            <CardTitle>
                                { event.title }

                            </CardTitle>
                            <NavLink to={ `/organization/${ event.eventOrganization.slug }` }
                                     className={ "hover:underline underline-offset-4 hover:text-primary" }>
                                <Text styleVariant={ "smallMuted" } className={ "leading-5 hover:text-primary" }>
                                    by { event.eventOrganization?.name }
                                </Text>
                            </NavLink>
                            <Text styleVariant={ "smallMuted" }>
                                { event.location?.locationName }
                            </Text>
                        </div>
                        <EventCalendarSheet isoDate={ event.eventDateTime }
                                            wrapperClassName={ "p-0.5 px-2" }
                                            dayClassName={ "md:text-xs" }
                                            dateNumberClassName={ "md:text-base" }
                                            monthClassName={ "md:text-xs" }
                        />
                    </div>
                </CardHeader>
                <div className={ "px-3 flex justify-between" }>
                    <EventBadges event={ event } badgeClassNames={ "px-1 py-0" }/>
                    <CardAction className={ "ml-auto" }>
                        <Button asChild>
                            <NavLink to={ `/event/${ event.id }` }>
                                Book Now
                            </NavLink>
                        </Button>
                    </CardAction>
                </div>
            </Card>
        </div>
    )
}
