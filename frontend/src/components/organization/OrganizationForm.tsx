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
import { Button } from "@/components/ui/button.tsx";
import { useState } from "react";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import LocationFormPart from "@/components/shared/LocationFormPart.tsx";
import ImageFormPart from "@/components/shared/ImageFormPart.tsx";

type OrganizationFormData = {
    name: string;
    description?: string;
    website?: string;
    address: string;
    city: string;
    zipCode: string;
    country: string;
    latitude?: number;
    longitude?: number;
    email?: string;
    phoneNumber?: string;
}

const formSchema = z.object( {
    name: z
        .string()
        .min( 3, "Organization name must be at least 3 characters" )
        .max( 30, "Organization name must be at most 50 characters" )
        .trim(),
    description: z
        .string()
        .min( 4, "Description must be at least 4 characters" )
        .max( 1500, "Description must be at most 500 characters" )
        .optional()
        .or( z.literal( "" ) ),
    website: z.url().optional().or( z.literal( `` ) ),
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
    email: z.email().trim().optional().or( z.literal( `` ) ),
    phoneNumber: z
        .string()
        .optional(),
} )

type OrganizationFormProps = {
    organizationData?: OrganizationResponseDto,
    onSubmit: ( organizationDto: OrganizationRequestDto, imageFile: File | null, deleteImage: boolean | undefined ) => void,
}


export default function OrganizationForm( {
                                              organizationData,
                                              onSubmit
                                          }: Readonly<OrganizationFormProps> ) {

    const { data: imageData } = useGetImageAsDataUrl( organizationData?.imageId || "", {
        query: {
            enabled: organizationData?.imageId !== null
        }
    } )

    const [ imageFile, setImageFile ] = useState<File | null>( null );
    const [ isRemovingImage, setIsRemovingImage ] = useState<boolean>( false );

    const form = useForm<OrganizationFormData>( {
        resolver: zodResolver( formSchema ),
        defaultValues: {
            name: organizationData?.name || "",
            description: organizationData?.description || "",
            website: organizationData?.website || "",
            address: organizationData?.location?.address || "",
            city: organizationData?.location?.city || "",
            zipCode: organizationData?.location?.zipCode || "",
            country: organizationData?.location?.country || "",
            latitude: organizationData?.location?.latitude || undefined,
            longitude: organizationData?.location?.longitude || undefined,
            email: organizationData?.contact?.email || "",
            phoneNumber: organizationData?.contact?.phoneNumber || "",
        }
    } );

    const isImageDeletion = !!organizationData?.imageId && isRemovingImage && !imageFile;
    const suppressSubmit = ( !form.formState.isDirty && !imageFile && !isImageDeletion ) || form.formState.isSubmitting;

    const handleSubmit = ( data: OrganizationFormData ) => {
        if ( suppressSubmit ) {
            console.log( "Form is dirty or is submitting, returning early." );
            return;
        }

        const organizationDto: OrganizationRequestDto = {
            name: data.name,
            description: data.description || undefined,
            website: data.website || undefined,
            location: {
                address: data.address || "",
                city: data.city || "",
                zipCode: data.zipCode || "",
                country: data.country || "",
                latitude: data.latitude || 0,
                longitude: data.longitude || 0,
            },
            contact: {
                email: data.email || undefined,
                phoneNumber: data.phoneNumber || undefined,
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
        <form onSubmit={ form.handleSubmit( handleSubmit ) }>
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
                        name={ "name" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Organization Name<span className={ "text-red-400" }>*</span>
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
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
                        name={ "website" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Website URL
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
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
                <LocationFormPart form={ form }/>
                <FieldSeparator/>
                <FieldSet>
                    <FieldLabel>
                        Contact
                    </FieldLabel>
                    <Controller
                        name={ "email" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Email
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
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
                        name={ "phoneNumber" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Phone Number
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
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
                <Button className={ "ml-auto" } type={ "submit" }>
                    Submit
                </Button>
            </FieldGroup>
        </form>
    )
}
