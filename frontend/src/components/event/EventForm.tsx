import { z } from "zod";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { AppUserDto, Event, EventRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import LocationFormPart from "@/components/shared/LocationFormPart.tsx";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldSeparator } from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { useState } from "react";
import ImageFormPart from "@/components/shared/ImageFormPart.tsx";
import { createEventBody } from "@/api/generated/event-controller/event-controller.zod.ts";
import ButtonWithLoading from "@/components/shared/ButtonWithLoading.tsx";

const extendedEventBody = createEventBody.extend( {
    event: createEventBody.shape.event.extend( {
            eventDateTime: z.preprocess( ( dateTime: string ) => {
                if ( dateTime.length == 0 ) return "";
                return new Date( dateTime ).toISOString();
            }, z.iso.datetime( "Date and time are required" ) )
                .refine( ( date ) => {
                    const now = new Date();
                    const eventTime = new Date( date );
                    return now < eventTime;
                }, { error: "Must be in the future" } ),
            maxTicketCapacity: z.coerce.number<number>()
                .positive( "Max ticket capacity must be positive" )
                .optional(),
            maxPerBooking: z.coerce.number<number>()
                .positive( "Max tickets per booking must be positive" )
                .optional(),
            price: z.coerce.number<number>()
                .nonnegative( "Price cannot be negative" ),
        }
    )
} );

type EventFormData = z.infer<typeof extendedEventBody>

type EventFormProps = {
    eventData?: Event;
    user: AppUserDto;
    onSubmit: ( data: EventRequestDto, file: File | null, deleteImage: boolean ) => void;
}

export default function EventForm( { eventData, user, onSubmit }: Readonly<EventFormProps> ) {

    const [ isRemovingImage, setIsRemovingImage ] = useState<boolean>( false );


    const { data: imageData } = useGetImageAsDataUrl( eventData?.imageId || "", {
        query: {
            enabled: eventData?.imageId !== null && eventData?.imageId !== undefined
        }
    } )


    const [ imageFile, setImageFile ] = useState<File | null>( null );

    const form = useForm<EventFormData>( {
        resolver: zodResolver( extendedEventBody ),
        defaultValues: {
            event: {
                organizationId: eventData?.eventOrganization.id || undefined,
                title: eventData?.title || "",
                description: eventData?.description || "",
                eventDateTime: eventData?.eventDateTime
                    ? new Date( eventData.eventDateTime ).toISOString().slice( 0, 16 )
                    : "",
                price: eventData?.price || 0,
                maxTicketCapacity: eventData?.maxTicketCapacity || undefined,
                maxPerBooking: eventData?.maxPerBooking || undefined,
                location: {

                    address: eventData?.location?.address || "",
                    city: eventData?.location?.city || "",
                    zipCode: eventData?.location?.zipCode || "",
                    country: eventData?.location?.country || "",
                }
            },
        }
    } )

    const isImageDeletion = !!eventData?.imageId && isRemovingImage && !imageFile;
    const suppressSubmit = ( ( !form.formState.isValid || !form.formState.isDirty ) && !imageFile && !isImageDeletion ) || form.formState.isSubmitting;

    const handleSubmit = ( data: EventFormData ) => {


        if ( suppressSubmit ) {
            console.log( "Form state invalid, returning early." );
            return;
        }

        const eventRequestDto: EventRequestDto = {
            organizationId: data.event.organizationId,
            title: data.event.title,
            description: data.event.description,
            eventDateTime: data.event.eventDateTime,
            location: {
                locationName: data.event.location.locationName,
                address: data.event.location.address,
                city: data.event.location.city,
                zipCode: data.event.location.zipCode,
                country: data.event.location.country,
            },
            price: data.event.price,
            maxTicketCapacity: data.event.maxTicketCapacity || undefined,
            maxPerBooking: data.event.maxPerBooking || undefined,
        };

        onSubmit( eventRequestDto, imageFile, isImageDeletion );
    }

    const handleImageFile = ( file: File | null ) => {
        if ( file === null ) {
            setImageFile( null );
        } else {
            setImageFile( file );
        }
    }

    const handleTrackImageRemoval = ( isRemoving: boolean ) => {
        setIsRemovingImage( isRemoving );
    }


    return (
        <form onSubmit={ form.handleSubmit( handleSubmit ) } className={ "space-y-6" }>
            <FieldGroup>
                <ImageFormPart labelText={ "Event Image Banner" }
                               imageData={ imageData?.data }
                               imageFile={ imageFile }
                               setImageFile={ handleImageFile }
                               trackImageRemoval={ handleTrackImageRemoval }/>
                <FieldSeparator/>
                <Controller
                    name={ "event.organizationId" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Organization<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Select
                                    name={ field.name }
                                    value={ field.value }
                                    onValueChange={ field.onChange }>
                                    <SelectTrigger
                                        aria-invalid={ fieldState.invalid }>
                                        <SelectValue placeholder={ "Select Organization" }/>
                                    </SelectTrigger>
                                    <SelectContent>
                                        { user.organizations?.map( ( orga ) => {
                                            return (
                                                <SelectItem key={ orga.id } value={ orga.id }>
                                                    { orga.name }
                                                </SelectItem>
                                            )
                                        } ) }
                                    </SelectContent>
                                </Select>
                                <FieldDescription>
                                    Choose the organization hosting the event
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.title" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Title<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    aria-invalid={ fieldState.invalid }
                                    placeholder={ "Title of the event.." }
                                />
                                <FieldDescription>
                                    Required
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.description" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Description
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Textarea
                                    { ...field }
                                    rows={ 4 }
                                    aria-invalid={ fieldState.invalid }
                                    placeholder={ "Describe the event..." }
                                />
                                <FieldDescription>
                                    Max. 1500 characters
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.eventDateTime" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Date / Time<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    type={ "datetime-local" }
                                    { ...field }
                                    aria-invalid={ fieldState.invalid }
                                />
                                <FieldDescription>
                                    Set the date and time of the event.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.maxTicketCapacity" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Max. Ticket Capacity
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    type={ "number" }
                                    aria-invalid={ fieldState.invalid }
                                    onChange={ ( e ) => e.target.value === "" ? field.onChange( undefined ) : field.onChange( e.target.value ) }
                                />
                                <FieldDescription>
                                    Not required. Leave empty for unlimited capacity.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.price" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Price per Ticket (€)<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    type={ "number" }
                                    step="0.01"
                                    aria-invalid={ fieldState.invalid }
                                />
                                <FieldDescription>
                                    Required. Set to 0 for free events.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "event.maxPerBooking" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Tickets per Booking
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto text-wrap" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    type={ "number" }
                                    defaultValue={ 1 }
                                    aria-invalid={ fieldState.invalid }
                                    onChange={ ( e ) => e.target.value === "" ? field.onChange( undefined ) : field.onChange( e.target.value ) }

                                />
                                <FieldDescription>
                                    Not required. Maximum number of tickets that can be booked per person.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <LocationFormPart form={ form } basePath={ "event.location" }/>
                <ButtonWithLoading disabled={ form.formState.isSubmitting } isLoading={ form.formState.isSubmitting }
                                   className={ "max-w-[100px] ml-auto" }>
                    Submit
                </ButtonWithLoading>
            </FieldGroup>
        </form>
    )
}
