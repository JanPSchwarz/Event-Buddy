import PageWrapper from "@/components/PageWrapper.tsx";
import { NavLink, useParams } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";
import { useGetUserById } from "@/api/generated/user/user.ts";
import MainHeading from "@/components/shared/MainHeading.tsx";
import Text from "@/components/typography/Text.tsx";
import OrganizationCard from "@/components/organization/OrganizationCard.tsx";
import { useGetEventsByUserId } from "@/api/generated/event-controller/event-controller.ts";
import EventCard from "@/components/event/EventCard.tsx";
import { Button } from "@/components/ui/button.tsx";

export default function DashboardPage() {

    const { userId } = useParams();

    const { user } = useContextUser();

    const { data: userDto } = useGetUserById( user.id, {
        query: {
            enabled: !!userId
        }
    } );

    const { data: eventsByUser } = useGetEventsByUserId( user.id, {
        query: {
            enabled: !!userId
        }
    } );

    return (
        <PageWrapper className={ "mb-12" }>
            <MainHeading heading={ "Your Dashboard" } subheading={ user.name }/>
            <div className={ "w-4/5 space-y-4" }>
                <div className={ "flex flex-col gap-2" }>
                    <Text className={ "border-b" }>
                        Your Orgas
                    </Text>
                    <Button variant={ "outline" } size={ "sm" } className={ "ml-auto" } asChild>
                        <NavLink to={ "/organization/create" }>
                            Create New Orga
                        </NavLink>
                    </Button>
                </div>
                { ( !userDto?.data.organizations || userDto?.data?.organizations?.length === 0 ) &&
                    <Text className={ "text-center" }>
                        No organizations to display.
                    </Text>
                }
                {
                    userDto?.data && userDto?.data?.organizations?.length > 0 &&
                    <div className={ "flex gap-6 justify-evenly" }>
                        { userDto?.data?.organizations?.map( ( orga ) => (
                            <OrganizationCard key={ orga.id } orgData={ orga }/>
                        ) ) }
                    </div>
                }
            </div>
            {
                eventsByUser?.data && eventsByUser?.data?.length > 0 &&
                <div className={ "w-4/5 space-y-4 mt-8" }>
                    <div className={ "flex flex-col gap-2" }>
                        <Text className={ "border-b" }>
                            Your Events
                        </Text>
                        <Button variant={ "outline" } size={ "sm" } className={ "ml-auto" } asChild>
                            <NavLink to={ "/event/create" }>
                                Create New Event
                            </NavLink>
                        </Button>
                    </div>
                    <div className={ "flex gap-8 items-center flex-wrap justify-evenly" }>
                        { eventsByUser?.data.map( ( event ) => (
                            <div key={ event.id } className={ "border p-4 rounded-md" }>
                                <EventCard key={ event.id } event={ event }/>
                                <div className={ "flex justify-between mt-4" }>
                                    <Button variant={ "secondary" } asChild>
                                        <NavLink to={ `/dashboard/event/${ event.id }` }>
                                            Manage
                                        </NavLink>
                                    </Button>
                                    <Button variant={ "secondary" } asChild>
                                        <NavLink to={ `/event/edit/${ event.id }` }>
                                            Edit
                                        </NavLink>
                                    </Button>
                                </div>
                            </div>
                        ) ) }
                    </div>
                </div>
            }
        </PageWrapper>
    )
}
