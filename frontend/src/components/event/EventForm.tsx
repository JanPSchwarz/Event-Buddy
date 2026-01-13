import { z } from "zod";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { AppUserDto, Event, EventRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import LocationFormPart from "@/components/shared/LocationFormPart.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldSeparator } from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { useState } from "react";
import ImageFormPart from "@/components/shared/ImageFormPart.tsx";

const formSchema = z.object( {

    organizationId: z.string().nonempty( "Organization is required" ),

    title: z.string()
        .min( 1, "Title is required" )
        .max( 50, "Title must be at most 50 characters" ),

    description: z.string()
        .max( 1500, "Description must be at most 1500 characters" )
        .optional()
        .or( z.literal( "" ) ),

    eventDateTime: z.preprocess( ( dateTime: string ) => {
        if ( dateTime.length == 0 ) return "";
        return new Date( dateTime ).toISOString();
    }, z.iso.datetime( "Date and time are required" ) )
        .refine( ( date ) => {
            const now = new Date();
            const eventTime = new Date( date );
            console.log( now > eventTime );
            return now < eventTime;
        }, { error: "Must be in the future" } ),

    locationName: z.string().optional(),

    address: z
        .string()
        .min( 1, "Address is required" )
        .max( 30, "Address must be at most 30 characters" ),
    city: z
        .string()
        .min( 1, "City is required" )
        .max( 30, "City must be at most 30 characters" ),
    zipCode: z.string()
        .min( 1, "Zip Code is required" ),
    country: z.string()
        .min( 4, "Country must be at least 4 characters" )
        .max( 30, "Country must be at most 30 characters" ),

    price: z.coerce.number<number>()
        .nonnegative( "Price cannot be negative" ),

    maxTicketCapacity: z.coerce.number<number>()
        .positive( "Max ticket capacity must be positive" )
        .optional(),

    maxPerBooking: z.coerce.number<number>()
        .positive( "Max tickets per booking must be positive" )
        .optional(),

} )

type EventFormData = z.infer<typeof formSchema>

type EventFormProps = {
    eventData?: Event;
    user: AppUserDto;
    onSubmit: ( data: EventRequestDto, file: File | null, deleteImage: boolean ) => void;
}

export default function EventForm( { eventData, user, onSubmit }: Readonly<EventFormProps> ) {

    const [ isRemovingImage, setIsRemovingImage ] = useState<boolean>( false );


    const { data: imageData } = useGetImageAsDataUrl( eventData?.imageId || "", {
        query: {
            enabled: eventData?.imageId !== null
        }
    } )


    const [ imageFile, setImageFile ] = useState<File | null>( null );

    const form = useForm<EventFormData>( {
        resolver: zodResolver( formSchema ),
        defaultValues: {
            organizationId: eventData?.eventOrganization.id || "",
            title: eventData?.title || "",
            description: eventData?.description || "",
            eventDateTime: eventData?.eventDateTime
                ? new Date( eventData.eventDateTime ).toISOString().slice( 0, 16 )
                : "",
            address: eventData?.location?.address || "",
            city: eventData?.location?.city || "",
            zipCode: eventData?.location?.zipCode || "",
            country: eventData?.location?.country || "",
            price: eventData?.price || 0,
            maxTicketCapacity: eventData?.maxTicketCapacity || undefined,
            maxPerBooking: eventData?.maxPerBooking || undefined,
        }
    } )

    const isImageDeletion = !!eventData?.imageId && isRemovingImage && !imageFile;
    const suppressSubmit = ( !form.formState.isDirty && !imageFile && !isImageDeletion ) || form.formState.isSubmitting;

    const handleSubmit = ( data: EventFormData ) => {

        if ( suppressSubmit ) {
            console.log( "Form is dirty or is submitting, returning early." );
            return;
        }

        const eventRequestDto: EventRequestDto = {
            organizationId: data.organizationId,
            title: data.title,
            description: data.description,
            eventDateTime: data.eventDateTime,
            location: {
                locationName: data.locationName,
                address: data.address,
                city: data.city,
                zipCode: data.zipCode,
                country: data.country,
            },
            price: data.price,
            maxTicketCapacity: data.maxTicketCapacity || undefined,
            maxPerBooking: data.maxPerBooking || undefined,
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
                    name={ "organizationId" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Organization<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
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
                    name={ "title" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Title<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
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
                    name={ "description" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Description
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
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
                    name={ "eventDateTime" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Date / Time<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
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
                    name={ "maxTicketCapacity" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Max. Ticket Capacity
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    type={ "number" }
                                    aria-invalid={ fieldState.invalid }
                                />
                                <FieldDescription>
                                    Not required. Leave empty for unlimited capacity.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <Controller
                    name={ "price" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Price per Ticket (€)<span className={ "text-red-400" }>*</span>
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
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
                    name={ "maxPerBooking" }
                    control={ form.control }
                    render={
                        ( { field, fieldState } ) => (
                            <Field data-invalid={ fieldState.invalid }>
                                <FieldLabel>
                                    Tickets per Booking
                                    { fieldState.invalid && (
                                        <FieldError className={ "text-xs ml-auto leading-0" }
                                                    errors={ [ fieldState.error ] }/>
                                    ) }
                                </FieldLabel>
                                <Input
                                    { ...field }
                                    type={ "number" }
                                    defaultValue={ 1 }
                                    aria-invalid={ fieldState.invalid }
                                />
                                <FieldDescription>
                                    Not required. Maximum number of tickets that can be booked per person.
                                </FieldDescription>
                            </Field>
                        )
                    }
                />
                <LocationFormPart form={ form }/>
                <Button className={ "max-w-[100px] ml-auto" }>
                    Submit
                </Button>
            </FieldGroup>
        </form>
    )
}
