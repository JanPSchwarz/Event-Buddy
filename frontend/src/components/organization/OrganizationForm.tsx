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
import type { Organization, OrganizationRequestDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Textarea } from "@/components/ui/textarea.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { ImagePlusIcon } from 'lucide-react';
import { type ChangeEvent, useRef, useState } from "react";
import { toast } from "sonner";

type OrganizationFormData = {
    name: string;
    description?: string;
    website?: string;
    address?: string;
    city?: string;
    zipCode?: string;
    country?: string;
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
        .max( 500, "Description must be at most 500 characters" )
        .optional()
        .or( z.literal( "" ) ),
    website: z.url().optional().or( z.literal( `` ) ),
    address: z
        .string()
        .max( 30, "Address must be at most 30 characters" )
        .optional()
        .or( z.literal( `` ) ),
    city: z
        .string()
        .max( 30, "City must be at most 30 characters" )
        .optional()
        .or( z.literal( `` ) ),
    zipCode: z.string().optional().or( z.literal( `` ) ),
    country: z.string()
        .min( 4, "Country must be at least 4 characters" )
        .max( 30, "Country must be at most 30 characters" )
        .optional()
        .or( z.literal( `` ) ),
    email: z.email().trim().optional().or( z.literal( `` ) ),
    phoneNumber: z
        .string()
        .optional(),
} )

type OrganizationFormProps = {
    organizationData?: Organization,
    onSubmit: ( organizationDto: OrganizationRequestDto, imageFile: File | null ) => void,
}


export default function OrganizationForm( { organizationData, onSubmit }: Readonly<OrganizationFormProps> ) {

    const [ selectedImageData, setSelectedImageData ] = useState<string | null>( null );
    const [ imageFile, setImageFile ] = useState<File | null>( null );
    const fileInputRef = useRef<HTMLInputElement>( null );

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

    const suppressSubmit = !form.formState.isDirty || form.formState.isSubmitting;


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


        onSubmit( organizationDto, imageFile );
    }

    const handleImageClick = () => {
        fileInputRef.current?.click();
    }

    const handleImageChange = ( event: ChangeEvent<HTMLInputElement> ) => {
        const file = event.target.files?.[ 0 ];

        if ( !file ) return;

        if ( file.size > 5 * 1024 * 1024 ) {
            toast.error( "Image size is too large" );
            return;
        }

        const allowedTypes = [ "image/jpeg", "image/png", "image/svg+xml", "image/heic", "image/webp" ];

        if ( !allowedTypes.includes( file.type ) ) {
            toast.error( "Image type not supported" );
            return;
        }

        setImageFile( file );

        const reader = new FileReader();
        reader.onload = ( e ) => {
            setSelectedImageData( e.target?.result as string );
        };
        reader.readAsDataURL( file );

    }

    const handleImageSize = ( size: number | undefined ) => {
        if ( !size ) return "";

        if ( size < 1024 ) {
            return `${ size } B`;
        } else if ( size < 1024 * 1024 ) {
            return `${ ( size / 1024 ).toFixed( 1 ) } KB`;
        } else {
            return `${ ( size / ( 1024 * 1024 ) ).toFixed( 1 ) } MB`;
        }
    }

    const handleRemoveImage = () => {
        setImageFile( null );
        setSelectedImageData( "" );
    }


    return (
        <form onSubmit={ form.handleSubmit( handleSubmit ) }>
            <FieldGroup>
                <FieldSeparator/>
                <FieldSet>
                    <FieldLabel>
                        Your Logo
                    </FieldLabel>
                    <Avatar
                        aria-label={ "click to select image logo for upload" }
                        onClick={ handleImageClick }
                        className={ "mx-auto object-contain border cursor-pointer size-36 my-4" }
                    >
                        <AvatarImage className={ "" } src={ selectedImageData || "" }/>
                        <AvatarFallback>
                            <ImagePlusIcon className={ "size-8" }/>
                        </AvatarFallback>
                    </Avatar>
                    { imageFile &&
                        <Button onClick={ handleRemoveImage }
                                variant={ "outline" }
                                type={ "button" }
                                className={ "max-w-[200px] mx-auto" }>
                            Remove Image
                        </Button>
                    }
                    <input
                        ref={ fileInputRef }
                        type={ "file" }
                        onChange={ handleImageChange }
                        accept={ "image/jpeg,image/png,image/svg+xml,image/heic,image/webp" }
                        className={ "hidden" }/>
                    <FieldDescription>
                        { imageFile &&
                            <span className={ "my-2 block" }>
                                    { imageFile?.name } ({ handleImageSize( imageFile?.size ) })
                                </span> }
                        Max. 5MB. jpeg, png, svg, heic, webp only
                    </FieldDescription>
                </FieldSet>
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
                                        Organization Name
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
                                        Max. 500 characters, not required
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
                <FieldSet>
                    <FieldLabel>Address and Location</FieldLabel>
                    <Controller
                        name={ "address" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Street name and number
                                        { fieldState.invalid && (
                                            <FieldError className={ "text-xs ml-auto leading-0" }
                                                        errors={ [ fieldState.error ] }/>
                                        ) }
                                    </FieldLabel>
                                    <Input
                                        { ...field }
                                        aria-invalid={ fieldState.invalid }
                                        placeholder={ "Example drive 15" }
                                    />
                                </Field>
                            )
                        }
                    />
                    <Controller
                        name={ "city" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        City
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
                        name={ "zipCode" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Zip-Code
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
                        name={ "country" }
                        control={ form.control }
                        render={
                            ( { field, fieldState } ) => (
                                <Field data-invalid={ fieldState.invalid }>
                                    <FieldLabel>
                                        Country
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
