import { z } from "zod";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { AppUser } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useMakeBooking } from "@/api/generated/booking-controller/booking-controller.ts";
import { toast } from "sonner";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import Text from "@/components/typography/Text.tsx";
import { InfoIcon } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";

const formSchema = z.object( {
    numberOfTickets: z.coerce.number<number>().positive()
        .int( "must be an integer" )
        .min( 1, "At least one ticket must be booked" )
    ,
    name: z.string()
        .min( 1, "Name is required" )

} )

type EventFormData = z.infer<typeof formSchema>;

type BookingFormData = {
    user: AppUser;
    eventId: string;
    bookingCompleted: () => void;
    isSoldOut?: boolean;
    price?: number;
}

export default function BookingForm( {
                                         user,
                                         isSoldOut,
                                         eventId,
                                         bookingCompleted,
                                         price
                                     }: Readonly<BookingFormData> ) {

    const form = useForm<EventFormData>( {
        resolver: zodResolver( formSchema ),
        defaultValues: {
            numberOfTickets: 1,
            name: user.name,
        }
    } );

    const queryClient = useQueryClient();

    const bookEvent = useMakeBooking( {
        axios:
            {
                withCredentials: true,
            }
    } )
    const values = form.watch();

    const suppressSubmit = form.formState.isSubmitting || !form.formState.isValid || isSoldOut

    const handleSubmit = ( data: EventFormData ) => {

        if ( suppressSubmit ) {
            console.log( "Cannot submit" );
            return;
        }

        bookEvent.mutate(
            {
                data: {
                    eventId,
                    name: data.name,
                    numberOfTickets: data.numberOfTickets,
                    userId: user.id,
                }
            },
            {
                onSuccess: ( response ) => {
                    console.log( "Booking successful", response );
                    toast.success( "Event booked successfully!" );
                    queryClient.invalidateQueries().then( () => {
                        bookingCompleted();
                    } );
                },
                onError: ( error ) => {
                    console.error( "Booking failed", error );
                    toast.error( error.response?.data.error || "Error booking tickets" );
                }
            }
        )
    }

    const bookedTickets = values.numberOfTickets;
    const summedUpPrice = price ? price * bookedTickets : null;

    return (
        <div className={ "relative" }>
            {
                bookEvent.isPending &&
                <CustomLoader className={ "absolute -translate-y-1/2 top-1/2" }/>
            }
            <form onSubmit={ form.handleSubmit( handleSubmit ) }
                  className={ `space-y-6 ${ bookEvent.isPending && "opacity-20" }` }>
                <FieldGroup>
                    <Controller
                        name={ "name" }
                        control={ form.control }
                        disabled={ isSoldOut }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Name<span className={ "text-red-400" }>*</span>
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "Name for booking.." }
                                    />
                                    <FieldDescription>
                                        Required
                                    </FieldDescription>
                                </Field>
                            )
                        }
                    />
                    <Controller
                        name={ "numberOfTickets" }
                        control={ form.control }
                        disabled={ isSoldOut }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Number of Tickets<span className={ "text-red-400" }>*</span>
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
                                        Required
                                    </FieldDescription>
                                </Field>
                            )
                        }
                    />
                </FieldGroup>
                <Button disabled={ suppressSubmit }>
                    Submit
                </Button>
            </form>

            <div className={ "w-full grid grid-cols-2 mt-8" }>
                <Text>
                    Price:
                </Text>
                <Text>
                    { summedUpPrice ? `${ summedUpPrice.toFixed( 2 ) } €` : "Free" }
                </Text>
                { price !== 0 &&
                    <Text styleVariant={ "smallMuted" } className={ "col-span-2 mt-2" }>
                        <InfoIcon className={ "inline mr-2" }/>
                        Payment is going to be handled on-site.
                    </Text>
                }
            </div>
        </div>
    )
}
