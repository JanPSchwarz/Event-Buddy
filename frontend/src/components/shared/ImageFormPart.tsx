import { FieldDescription, FieldLabel, FieldSet } from "@/components/ui/field.tsx";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { ImagePlusIcon, InfoIcon } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import { type ChangeEvent, useRef, useState } from "react";
import { toast } from "sonner";
import EventImage from "@/components/event/EventImage.tsx";
import Text from "@/components/typography/Text.tsx";
import { Badge } from "@/components/ui/badge.tsx";

type ImageFormPartProps = {
    imageData?: string;
    setImageFile: ( file: File | null ) => void;
    imageFile?: File | null;
    labelText?: string;
    avatar?: boolean;
    trackImageRemoval: ( removed: boolean ) => void;
}

export default function ImageFormPart( {
                                           imageData,
                                           setImageFile,
                                           imageFile,
                                           labelText,
                                           avatar,
                                           trackImageRemoval
                                       }: Readonly<ImageFormPartProps> ) {

    const [ imageSource, setImageSource ] = useState<string | null>( imageData || null );
    const fileInputRef = useRef<HTMLInputElement>( null );

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
            setImageSource( e.target?.result as string );
            trackImageRemoval( false );
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
        setImageSource( "" );
        trackImageRemoval( true );
    }

    return (
        <FieldSet>
            <FieldLabel>
                { labelText || "Your Image" }
            </FieldLabel>
            { avatar ?
                <Avatar
                    role={ "button" }
                    aria-label={ "click to select image logo for upload" }
                    tabIndex={ 0 }
                    onKeyDown={ ( e ) => {
                        if ( e.key === 'Enter' || e.key === ' ' ) {
                            e.preventDefault();
                            handleImageClick();
                        }
                    } }
                    onClick={ handleImageClick }
                    className={ "mx-auto object-contain border cursor-pointer size-36 my-4" }
                >
                    <AvatarImage className={ "" } src={ imageSource || "" }/>
                    <AvatarFallback>
                        <ImagePlusIcon aria-hidden={ true } className={ "size-8" }/>
                    </AvatarFallback>
                </Avatar>
                :
                <div
                    role={ "button" }
                    className={ "cursor-pointer relative" }
                    aria-label={ "click to select image for upload" }
                    tabIndex={ 0 }
                    onKeyDown={ ( e ) => {
                        if ( e.key === 'Enter' || e.key === ' ' ) {
                            e.preventDefault();
                            handleImageClick();
                        }
                    } }
                    onClick={ handleImageClick }
                >
                    {
                        !imageSource &&
                        <Badge className={ "absolute top-1 right-2 z-10" }>
                            Default Image
                        </Badge>
                    }
                    <EventImage imageData={ imageSource }/>
                    <div className={ "flex items-center gap-2" }>
                        { !imageSource && <InfoIcon className={ "inline size-4 text-muted-foreground" }/> }
                        <Text styleVariant={ "smallMuted" }>
                            { !imageSource && "Default Image is used when no image is selected." }
                        </Text>
                    </div>
                </div>
            }
            <div className={ "space-x-4" }>
                <Button variant={ "outline" } className={ "mt-4" } type={ "button" } onClick={ handleImageClick }>
                    Change Image
                </Button>
                { imageSource &&
                    <Button onClick={ handleRemoveImage }
                            variant={ "outline" }
                            type={ "button" }
                            className={ "max-w-[200px] mx-auto" }>
                        Remove Image
                    </Button>
                }
            </div>
            <input
                id={ "imageUpload" }
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
                Max. 5MB. jpeg, png, svg, heic, webp only.
                <Text styleVariant={ "smallMuted" } asTag={ "span" } className={ "block" }>
                    Image ratio 16 / 9 recommended.
                </Text>
            </FieldDescription>
        </FieldSet>
    )
}
