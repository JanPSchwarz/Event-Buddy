import { Card, CardContent, CardHeader } from "@/components/ui/card.tsx";
import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { Spinner } from "@/components/ui/spinner.tsx";
import Text from "@/components/typography/Text.tsx";

type OrganizationViewProps = {
    orgaData: OrganizationResponseDto
}

export default function OrganizationView( { orgaData }: Readonly<OrganizationViewProps> ) {

    console.log( "Organization Data in OrganizationView component:", orgaData );

    const { data: imageData, isLoading: isLoadingImage } = useGetImageAsDataUrl( orgaData.imageId || "" )

    return (
        <Card className={ "w-full max-w-[600px] mx-auto" }>
            <CardHeader className={ "flex flex-col justify-center md:flex-row md:justify-between" }>
                <div>
                    <Text styleVariant={ "smallMuted" }>This is</Text>
                    <Text asTag={ "h2" } styleVariant={ "h2" }>{ orgaData.name }</Text>
                </div>
                { orgaData?.imageId &&
                    <Avatar
                        aria-label={ "click to select image logo for upload" }
                        className={ "object-contain mx-auto md:mx-0 border cursor-pointer size-36 my-4" }
                    >
                        <AvatarImage className={ "" } src={ imageData?.data }/>
                        <AvatarFallback>
                            {
                                isLoadingImage && <Spinner/>
                            }
                        </AvatarFallback>
                    </Avatar> }
            </CardHeader>
            <CardContent>
                Organization View
                <div className={ "grid grid-cols-2 gap-2 items-end" }>
                    {
                        orgaData?.description &&
                        <>
                            <Text>
                                Description:
                            </Text>
                            <Text>
                                { orgaData.description }
                            </Text></>
                    }

                </div>
            </CardContent>
        </Card>
    )
}
