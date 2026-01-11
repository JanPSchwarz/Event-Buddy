import { Controller, type FieldValues, type Path, type UseFormReturn } from "react-hook-form";
import { Field, FieldError, FieldLabel, FieldSet } from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";

type LocationFields = {
    locationName?: string;
    address: string;
    city: string;
    zipCode: string;
    country: string;
}

type LocationFormPartProps<T extends FieldValues & LocationFields> = {
    form: UseFormReturn<T>
}

export default function LocationFormPart<T extends FieldValues & LocationFields>( { form }: Readonly<LocationFormPartProps<T>> ) {


    return (
        <FieldSet>
            <FieldLabel>Address</FieldLabel>
            <Controller
                name={ "locationName" as Path<T> }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Location Name
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto leading-0" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "Maximus Event Hall" }
                            />
                        </Field>
                    )
                }
            />
            <Controller
                name={ "address" as Path<T> }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Street / number <span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto leading-0" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "Example Drive 15" }
                            />
                        </Field>
                    )
                }
            />
            <Controller
                name={ "city" as Path<T> }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                City<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto leading-0" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "Example City" }
                            />
                        </Field>
                    )
                }
            />
            <Controller
                name={ "zipCode" as Path<T> }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Zip-Code<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto leading-0" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "12345" }
                            />
                        </Field>
                    )
                }
            />
            <Controller
                name={ "country" as Path<T> }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Country<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto leading-0" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "12345" }
                            />
                        </Field>
                    )
                }
            />
        </FieldSet>
    )
}
