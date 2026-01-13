import { AspectRatio } from "@/components/ui/aspect-ratio.tsx";
import { twMerge } from "tailwind-merge";
import CustomLoader from "@/components/CustomLoader.tsx";

type EventImageProps = {
    imageData?: string | null,
    imageClassName?: string,
    isLoading?: boolean
}

export default function EventImage( {
                                        imageData,
                                        imageClassName,
                                        isLoading
                                    }: Readonly<EventImageProps> ) {

    const imageDataString = imageData || "/eventPlaceholder.svg";

    return (
        <div className={ "md:space-y-6" }>
            <AspectRatio ratio={ 16 / 9 } className={ `${ isLoading && "flex border rounded-t-md" }` }>
                {
                    isLoading ?
                        <CustomLoader text={ "Loading image..." }/>
                        :
                        <img src={ imageDataString }
                             className={ twMerge( "object-cover w-full h-full rounded-md m-auto", imageClassName ) }
                             alt={ "Event" }/>
                }
            </AspectRatio>
        </div>
    )
}
