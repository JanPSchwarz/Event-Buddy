import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card.tsx";
import EventForm from "@/components/event/EventForm.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { useGetUserById } from "@/api/generated/user/user.ts";
import { Navigate, useNavigate } from "react-router";
import { toast } from "sonner";
import { useCreateEvent } from "@/api/generated/event-controller/event-controller.ts";
import type { EventRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { useQueryClient } from "@tanstack/react-query";

export default function CreateEventForm() {

    const { user } = useContextUser();

    const navigate = useNavigate();

    const queryClient = useQueryClient();

    const createEvent = useCreateEvent( {
            axios: {
                withCredentials: true,
            }
        }
    );

    const { data: userData } = useGetUserById(
        user.id,
        {
            axios: {
                withCredentials: true,
            },
            query: {
                enabled: !!user?.id,
            }
        }
    );

    if ( !userData?.data ) {
        return null;
    }

    if ( !userData.data.organizations || userData.data?.organizations?.length === 0 ) {
        toast.info( "You need to have an organization to create an event. Please create an organization first." );
        return <Navigate to={ `/organization/create` }/>;
    }

    const handleSubmit = ( eventDto: EventRequestDto, imageFile: File | null ) => {
        createEvent.mutate(
            {
                data: {
                    event: eventDto,
                    imageFile: imageFile || undefined
                }
            },
            {
                onSuccess: ( response ) => {
                    toast.success( "Event created successfully!" );
                    queryClient.invalidateQueries().then( () => {
                        navigate( `/event/${ response.data.id }` );
                    } )
                },
                onError: ( error ) => {
                    console.error( "Error creating event:", error );
                    toast.error( error.response?.data.error || "Error creating event" );
                }
            }
        )


    }

    return (
        <Card className={ "md:p-4 w-full max-w-[900px] mx-auto" }>
            <CardHeader>
                <CardTitle>
                    Create Event
                </CardTitle>
                <CardDescription>
                    Create a new Event
                </CardDescription>
            </CardHeader>
            <CardContent>
                <EventForm user={ userData.data } onSubmit={ handleSubmit }/>
            </CardContent>
        </Card>
    )
}
