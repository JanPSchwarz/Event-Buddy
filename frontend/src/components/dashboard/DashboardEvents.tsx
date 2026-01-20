import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";
import type { EventResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import EventCard from "@/components/event/EventCard.tsx";
import { Plus } from "lucide-react";

type DashboardEventProps = {
    events: EventResponseDto[] | undefined,
    isLoading?: boolean,

}

export default function DashboardEvents( { events = [], isLoading }: Readonly<DashboardEventProps> ) {

    return (
        <div className={ "space-y-12" }>
            <div className={ "flex" }>
                <Button size={ "sm" } className={ "ml-auto" } asChild>
                    <NavLink to={ "/event/create" }>
                        <Plus/> Create Event
                    </NavLink>
                </Button>
            </div>
            {
                isLoading &&
                <CustomLoader/>
            }
            {
                !isLoading && events.length === 0 ?
                    <Text className={ "text-center" }>
                        No Events here yet.
                    </Text>
                    :
                    <div className={ "flex gap-8 items-center flex-wrap justify-start" }>
                        { events.map( ( event ) => (
                            <div key={ event.id } className={ "border p-4 rounded-md" }>
                                <EventCard key={ event.id } event={ event }/>
                                <Text styleVariant={ "smallMuted" }>Event id: { event.id }</Text>
                                <div className={ "flex justify-between mt-4" }>
                                    <Button asChild>
                                        <NavLink to={ `/dashboard/event/${ event.id }` }>
                                            Manage
                                        </NavLink>
                                    </Button>
                                    <Button variant={ "secondary" } asChild>
                                        <NavLink to={ `/event/edit/${ event.id }?src=dashboard` }>
                                            Edit
                                        </NavLink>
                                    </Button>
                                </div>
                            </div>
                        ) ) }
                    </div>

            }
        </div>
    )
}
