import { useNavigate, useParams, useSearchParams } from "react-router";
import { useGetRawEventById, useUpdateEvent } from "@/api/generated/event-controller/event-controller.ts";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import EventForm from "@/components/event/EventForm.tsx";
import { useGetUserById } from "@/api/generated/user/user.ts";
import { useContextUser } from "@/context/UserProvider.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import type { EventRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

export default function EditEventPage() {

    const { eventId } = useParams();

    const [ searchParams ] = useSearchParams();

    const source = searchParams.get( "src" );

    const { user } = useContextUser();

    const navigate = useNavigate();

    const { data: userData } = useGetUserById( user?.id || "", {
        axios: { withCredentials: true },
        query: {
            enabled: !!user?.id
        }
    } );

    const { data: eventData } = useGetRawEventById( eventId || "", {
        axios: { withCredentials: true },
        query: {
            enabled: !!eventId
        }
    } );

    const updateEvent = useUpdateEvent(
        {
            axios: {
                withCredentials: true,
            }
        }
    );

    const queryClient = useQueryClient();

    if ( !eventData || !userData ) {
        return null;
    }

    const getReturnPath = () => {
        switch ( source ) {
            case "dashboard":
                return `/dashboard/${ user?.id }`;
            case "manager":
                return `/dashboard/event/${ eventData.data.id }`;
            default:
                return `/event/${ eventData.data.id }`;
        }
    }


    const handleSubmit = ( eventFormData: EventRequestDto, imageFile: File | null, deleteImage: boolean ) => {


        if ( !eventData.data.id ) {
            console.error( "Event ID is missing. Cannot update event." );
            return;
        }

        updateEvent.mutate(
            {
                data: {
                    updateEvent: eventFormData,
                    imageFile: imageFile || undefined,
                    deleteImage
                },
                eventId: eventData.data.id,
            },
            {
                onSuccess: ( response ) => {
                    console.log( "Event updated successfully:", response );

                    toast.success( "Event updated successfully!" );
                    queryClient.invalidateQueries().then( () => {
                        navigate( getReturnPath() );
                    } );
                },
                onError: ( error ) => {
                    console.error( "Error updating event:", error );
                    toast.error( error.response?.data.error || "Error updating event" );
                }
            }
        );
    }


    return (
        <PageWrapper>
            <MainHeading heading={ "Edit Event" } subheading={ eventData?.data.title }/>
            <div className={ "w-full max-w-[1000px] px-12 mb-12" }>
                <EventForm user={ userData?.data } eventData={ eventData?.data } onSubmit={ handleSubmit }/>
            </div>
        </PageWrapper>
    )
}
