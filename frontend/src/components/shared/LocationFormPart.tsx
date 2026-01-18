import { Controller, type FieldValues, type Path, type UseFormReturn } from "react-hook-form";
import { Field, FieldError, FieldLabel, FieldSet } from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";

type LocationFormPartProps<T extends FieldValues> = {
    form: UseFormReturn<T>,
    basePath?: string
}

export default function LocationFormPart<T extends FieldValues>( {
                                                                     form,
                                                                     basePath = ""
                                                                 }: Readonly<LocationFormPartProps<T>> ) {

    const getFieldName = ( fieldName: string ): Path<T> => {
        return ( basePath ? `${ basePath }.${ fieldName }` : fieldName ) as Path<T>
    }

    return (
        <FieldSet>
            <FieldLabel>Address</FieldLabel>
            <Controller
                name={ getFieldName( "locationName" ) }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Location Name
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto" }
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
                name={ getFieldName( "address" ) }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Street / number <span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto" }
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
                name={ getFieldName( "city" ) }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                City<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto" }
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
                name={ getFieldName( "zipCode" ) }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Zip-Code<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto" }
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
                name={ getFieldName( "country" ) }
                control={ form.control }
                render={
                    ( { field, fieldState } ) => (
                        <Field data-invalid={ fieldState.invalid }>
                            <FieldLabel>
                                Country<span className={ "text-red-400" }>*</span>
                                { fieldState.invalid && (
                                    <FieldError className={ "text-xs ml-auto" }
                                                errors={ [ fieldState.error ] }/>
                                ) }
                            </FieldLabel>
                            <Input
                                { ...field }
                                aria-invalid={ fieldState.invalid }
                                placeholder={ "Germany" }
                            />
                        </Field>
                    )
                }
            />
        </FieldSet>
    )
}
