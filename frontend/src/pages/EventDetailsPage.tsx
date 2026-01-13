import CustomLoader from "@/components/CustomLoader.tsx";
import { useGetEventById } from "@/api/generated/event-controller/event-controller.ts";
import { NavLink, useParams } from "react-router";
import Text from "@/components/typography/Text.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import PageWrapper from "@/components/PageWrapper.tsx";
import EventCalendarSheet from "@/components/event/EventCalendarSheet.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { Button } from "@/components/ui/button.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import EventBadges from "@/components/event/EventBadges.tsx";
import EventImage from "@/components/event/EventImage.tsx";
import BookingDialog from "@/components/booking/BookingDialog.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { Alert, AlertDescription } from "@/components/ui/alert.tsx";

export default function EventDetailsPage() {

    const { eventId } = useParams();

    const { user } = useContextUser();

    const { data: eventData, isLoading: isLoadingEvent } = useGetEventById( eventId || "", {
        query: { enabled: !!eventId }
    } )

    const { data: imageData } = useGetImageAsDataUrl( eventData?.data.imageId || "", {
        query: { enabled: !!eventData?.data.imageId },
    } )

    if ( isLoadingEvent ) {
        return <CustomLoader size={ "size-6" } text={ "Loading..." }/>
    }

    if ( !eventData ) {
        return (
            <div className={ "w-full flex flex-col justify-center items-center gap-12" }>
                <Text asTag={ "h1" } styleVariant={ "h3" }
                      className={ "text-primary border-b border-muted-foreground" }>The Event you are looking for
                    does not exist</Text>
                <PageNotFound/>
            </div>
        )
    }

    const event = eventData?.data;


    const getTime = ( dateString: string ) => {
        const date = new Date( dateString );
        return date.toLocaleTimeString( "en-US", { hour: '2-digit', minute: '2-digit' } );
    }

    const getYear = ( dateString: string ) => {
        const thisYear = new Date().getFullYear();
        const date = new Date( dateString );

        if ( date.getFullYear() === thisYear ) {
            return "";
        } else {
            return "(" + date.getFullYear() + ")";
        }
    }

    const isOwner = user?.organizations?.includes( event.eventOrganization.id || "" );

    return (
        <PageWrapper className={ "max-w-[800px] mx-auto" }>
            {
                isOwner &&
                <div className={ "flex w-full justify-between items-center" }>
                    <Alert className={ "max-w-max mr-auto" }>
                        <AlertDescription>
                            You are viewing this event as an organizer. Go to your dashboard to manage it.
                        </AlertDescription>
                    </Alert>
                    <Button variant={ "outline" } asChild>
                        <NavLink to={ `/dashboard/event/${ event.id }` }>
                            Manager
                        </NavLink>
                    </Button>
                </div>
            }
            <Card className={ "w-full mb-12 mx-auto space-y-6 md:space-y-12" }>
                <CardHeader>
                    <EventBadges event={ event }/>
                    <CardTitle className={ "flex flex-col md:flex-row gap-6 justify-between" }>
                        <div>
                            <Text styleVariant={ "smallMuted" }>{ event.eventOrganization.name }</Text>
                            <Text asTag={ "h1" }
                                  styleVariant={ "h2" }>{ event.title } { getYear( ( event.eventDateTime || "" ) ) }
                            </Text>
                            <Text
                                styleVariant={ "smallMuted" }>
                                { event.location?.locationName ? `${ event.location.locationName }, ` : "" }
                                <Text asTag={ "span" }>
                                    { event.location?.city }
                                </Text>
                            </Text>
                        </div>
                        <EventCalendarSheet isoDate={ event.eventDateTime || "" }/>
                    </CardTitle>
                </CardHeader>
                <CardContent className={ "space-y-8" }>
                    <EventImage imageData={ imageData?.data }/>
                    <BookingDialog event={ eventData?.data }/>
                    <div className={ "space-y-4" }>
                        <Text styleVariant={ "h3" } className={ "text-muted-foreground" }>
                            Description
                        </Text>
                        <div className={ "space-y-4" }>
                            {
                                event.description ?
                                    event.description.split( ( `\n` ) ).map( ( paragraph, index ) => (
                                            <Text key={ index } className={ "" }>
                                                { paragraph }
                                            </Text>
                                        )
                                    ) :
                                    <Text styleVariant={ "smallMuted" }>
                                        "No description provided."
                                    </Text>
                            }
                        </div>
                    </div>
                    <Separator/>
                    <div className={ "grid grid-cols-2 items-center gap-4" }>
                        <Text styleVariant={ "h3" } className={ "text-muted-foreground" }>
                            Price
                            <Text asTag={ "span" }>
                                { " " }(per Ticket)
                            </Text>
                        </Text>
                        <Text>
                            { event.price != 0 ? `${ event.price.toFixed( 2 ) } €` : "Free" }
                        </Text>
                    </div>
                    <Separator/>
                    <div className={ "grid grid-cols-2 gap-4" }>
                        <Text styleVariant={ "h3" } className={ "text-muted-foreground col-span-2" }>
                            Where
                        </Text>
                        { event.location?.locationName &&
                            <>
                                <Text styleVariant={ "smallMuted" }>
                                    Location Name:
                                </Text>
                                <Text className={ "break-words" }>
                                    { event.location?.locationName }
                                </Text>
                            </>
                        }
                        <Text styleVariant={ "smallMuted" }>
                            Street/Number:
                        </Text>
                        <Text className={ "break-words" }>
                            { event.location?.address || "N/A" }
                        </Text>
                        <Text styleVariant={ "smallMuted" }>
                            City:
                        </Text>
                        <Text className={ "break-words" }>
                            { event.location?.city || "N/A" }
                        </Text>
                        <Text styleVariant={ "smallMuted" }>
                            Zip Code:
                        </Text>
                        <Text className={ "break-words" }>
                            { event.location?.zipCode || "N/A" }
                        </Text>
                    </div>
                    <Separator/>
                    <div className={ "grid grid-cols-2 items-center gap-4" }>
                        <Text styleVariant={ "h3" } className={ "text-muted-foreground col-span-2" }>
                            When
                        </Text>
                        <Text styleVariant={ "smallMuted" }>
                            Date:
                        </Text>
                        <Text>
                            { event.eventDateTime
                                ? `${ new Date( event.eventDateTime ).toLocaleDateString( "en-US", {
                                    year: 'numeric',
                                    month: 'long',
                                    day: '2-digit'
                                } ) }`
                                : "N/A"
                            }
                        </Text>
                        <Text styleVariant={ "smallMuted" }>
                            Time:
                        </Text>
                        <Text className={ "break-words" }>
                            { event.eventDateTime
                                ? `${ getTime( event.eventDateTime ) }`
                                : "N/A"
                            }
                        </Text>
                    </div>
                    <Separator/>
                    <div className={ "grid grid-cols-2 items-start justify-start gap-4" }>
                        <Text styleVariant={ "h3" } className={ "text-muted-foreground" }>
                            Organizer
                        </Text>
                        <Button asChild variant={ "link" } className={ "max-w-min p-0" }>
                            <NavLink to={ `/organization/${ event.eventOrganization.slug }` }>
                                { event.eventOrganization.name }
                            </NavLink>
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </PageWrapper>
    )
}
