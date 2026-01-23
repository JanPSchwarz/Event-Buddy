import { z } from "zod";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
    Field,
    FieldDescription,
    FieldError,
    FieldGroup,
    FieldLabel,
    FieldSeparator,
    FieldSet
} from "@/components/ui/field.tsx";
import { Input } from "@/components/ui/input.tsx";
import type { OrganizationRequestDto, OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Textarea } from "@/components/ui/textarea.tsx";
import { useState } from "react";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import LocationFormPart from "@/components/shared/LocationFormPart.tsx";
import ImageFormPart from "@/components/shared/ImageFormPart.tsx";
import { createOrganizationBody } from "@/api/generated/organization/organization.zod.ts";
import ButtonWithLoading from "@/components/shared/ButtonWithLoading.tsx";

const generatedFormSchema = createOrganizationBody;

type OrganizationFormData = z.infer<typeof generatedFormSchema>;

type OrganizationFormProps = {
    organizationData?: OrganizationResponseDto,
    onSubmit: ( organizationDto: OrganizationRequestDto, imageFile: File | null, deleteImage: boolean | undefined ) => void,
    isLoading?: boolean,
    formClassName?: string,
}


export default function OrganizationForm( {
                                              organizationData,
                                              onSubmit,
                                              isLoading,
                                              formClassName
                                          }: Readonly<OrganizationFormProps> ) {

    const { data: imageData } = useGetImageAsDataUrl( organizationData?.imageId || "", {
        query: {
            enabled: organizationData?.imageId !== null && organizationData?.imageId !== undefined
        }
    } )

    const [ imageFile, setImageFile ] = useState<File | null>( null );
    const [ isRemovingImage, setIsRemovingImage ] = useState<boolean>( false );

    const form = useForm<OrganizationFormData>( {
        resolver: zodResolver( generatedFormSchema ),
        defaultValues: {
            organization: {
                name: organizationData?.name || "",
                description: organizationData?.description || "",
                website: organizationData?.website || undefined,
                contact: {
                    email: organizationData?.contact?.email || undefined,
                    phoneNumber: organizationData?.contact?.phoneNumber || undefined,
                },
                location: {
                    address: organizationData?.location?.address || "",
                    city: organizationData?.location?.city || "",
                    zipCode: organizationData?.location?.zipCode || "",
                    country: organizationData?.location?.country || "",
                    latitude: organizationData?.location?.latitude || undefined,
                    longitude: organizationData?.location?.longitude || undefined,
                }
            }
        }
    } );

    const isImageDeletion = !!organizationData?.imageId && isRemovingImage && !imageFile;
    const suppressSubmit = ( ( !form.formState.isValid || !form.formState.isDirty ) && !imageFile && !isImageDeletion ) || form.formState.isSubmitting;

    const handleSubmit = ( data: OrganizationFormData ) => {

        if ( suppressSubmit ) {
            console.log( "Form is dirty or is submitting, returning early." );
            return;
        }

        const organizationDto: OrganizationRequestDto = {
            name: data.organization.name,
            description: data.organization.description || undefined,
            website: data.organization.website || undefined,
            location: {
                address: data.organization.location?.address || "",
                city: data.organization.location?.city || "",
                zipCode: data.organization.location?.zipCode || "",
                country: data.organization.location?.country || "",
                latitude: data.organization.location?.latitude || 0,
                longitude: data.organization.location?.longitude || 0,
            },
            contact: {
                email: data.organization.contact?.email || undefined,
                phoneNumber: data.organization.contact?.phoneNumber || undefined,
            }
        };


        onSubmit( organizationDto, imageFile, isImageDeletion );
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
        <form onSubmit={ form.handleSubmit( handleSubmit ) } className={ formClassName }>
            <FieldGroup>
                <FieldSeparator/>
                <ImageFormPart setImageFile={ handleImageFile }
                               imageData={ imageData?.data }
                               labelText={ "Your logo" }
                               avatar={ true }
                               trackImageRemoval={ handleTrackImageRemoval }/>
                <FieldSeparator/>
                <FieldSet>
                    <FieldLabel>
                        General
                    </FieldLabel>
                    <Controller
                        name={ "organization.name" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Organization Name<span className={ "text-red-400" }>*</span>
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "Organization Name..." }
                                    />
                                    <FieldDescription>
                                        Required
                                    </FieldDescription>
                                </Field>
                            )
                        }
                    />
                    <Controller
                        name={ "organization.description" }
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
                                        placeholder={ "Best Orga in town" }
                                    />
                                    <FieldDescription>
                                        Max. 1500 characters, not required
                                    </FieldDescription>
                                </Field>
                            )
                        }
                    />
                    <Controller
                        name={ "organization.website" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Website URL
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "https://my-orga.com" }
                                    />
                                    <FieldDescription>
                                        Enter external website URL, not required
                                    </FieldDescription>
                                </Field>
                            )
                        }
                    />
                </FieldSet>
                <FieldSeparator/>
                <LocationFormPart form={ form } basePath={ "organization.location" }/>
                <FieldSeparator/>
                <FieldSet>
                    <FieldLabel>
                        Contact
                    </FieldLabel>
                    <Controller
                        name={ "organization.contact.email" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Email
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "yourOrga@example.com" }
                                    />
                                </Field>
                            )
                        }
                    />
                    <Controller
                        name={ "organization.contact.phoneNumber" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Phone Number
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "54231" }
                                    />
                                </Field>
                            )
                        }
                    />
                </FieldSet>
                <ButtonWithLoading disabled={ form.formState.isSubmitting } isLoading={ isLoading }
                                   className={ "ml-auto" } type={ "submit" }>
                    Submit
                </ButtonWithLoading>
            </FieldGroup>
        </form>
    )
}
