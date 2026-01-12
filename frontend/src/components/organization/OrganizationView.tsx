import { Card, CardContent, CardHeader } from "@/components/ui/card.tsx";
import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { Spinner } from "@/components/ui/spinner.tsx";
import Text from "@/components/typography/Text.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import { User } from "lucide-react";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";

type OrganizationViewProps = {
    orgaData: OrganizationResponseDto,
}

export default function OrganizationView( { orgaData }: Readonly<OrganizationViewProps> ) {

    const { data: imageData, isLoading: isLoadingImage } = useGetImageAsDataUrl( orgaData.imageId || "" )

    return (
        <Card className={ "w-full max-w-[800px] mb-12 mx-auto" }>
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
            <CardContent className={ "space-y-6" }>
                <Separator/>
                <div className={ "space-y-4" }>
                    <Text asTag={ "h3" } styleVariant={ "h6" } className={ "text-muted-foreground" }>Description</Text>
                    { orgaData.description
                        ? orgaData.description.split( '\n' ).map( ( paragraph, index ) => (
                            <Text key={ index } className={ "" }>
                                { paragraph }
                            </Text>
                        ) )
                        : <Text className={ "" }>No description provided.</Text>
                    }
                    <Separator className={ "my-4" }/>
                </div>
                {
                    Object.values( orgaData?.location || {} )
                        .filter( ( value ) => value !== "longitude" && value !== "latitude" )
                        .some( ( contact ) => contact != null && contact != "" ) &&
                    <div className={ "space-y-4" }>
                        <Text asTag={ "h3" } className={ "text-muted-foreground" } styleVariant={ "h6" }>
                            Location
                        </Text>
                        <div className={ "grid grid-cols-2 gap-4" }>
                            {
                                orgaData.location?.address &&
                                <>
                                    <Text>Address:</Text>
                                    <Text className={ "break-words" }>{ orgaData.location.address }</Text>
                                </>
                            }
                            {
                                orgaData.location?.city &&
                                <>
                                    <Text>City:</Text>
                                    <Text className={ "break-words" }>{ orgaData.location.city }</Text>
                                </>
                            }
                            {
                                orgaData.location?.zipCode &&
                                <>
                                    <Text>Zip-Code:</Text>
                                    <Text className={ "break-words" }>{ orgaData.location.zipCode }</Text>
                                </>
                            }
                            {
                                orgaData.location?.country &&
                                <>
                                    <Text>Country:</Text>
                                    <Text className={ "break-words" }>{ orgaData.location.country }</Text>
                                </>
                            }
                        </div>
                        <Separator className={ "my-4" }/>
                    </div>
                }
                {
                    orgaData.website &&
                    <div className={ "space-y-4" }>
                        <Text asTag={ "h3" } className={ "text-muted-foreground" } styleVariant={ "h6" }>
                            Website (external)
                        </Text>
                        <Button asChild variant={ "link" } className={ "p-0 whitespace-break-spaces" }>
                            <a href={ orgaData.website } target={ "_blank" }
                               className={ "" }>
                                { orgaData.website }
                            </a>
                        </Button>
                        <Separator className={ "my-4" }/>
                    </div>
                }
                {
                    Object.values( orgaData?.contact || {} ).some( ( contact ) => contact != null && contact != "" ) &&
                    <div className={ "space-y-4" }>
                        <Text asTag={ "h3" } className={ "text-muted-foreground" } styleVariant={ "h6" }>
                            Contact
                        </Text>
                        <div className={ "grid grid-cols-2 gap-2" }>
                            {
                                orgaData.contact?.email &&
                                <>
                                    <Text className={ "break-words" }>Email:</Text>
                                    <Text className={ "break-words" }>{ orgaData.contact.email }</Text>
                                </>
                            }
                            {
                                orgaData.contact?.phoneNumber &&
                                <>
                                    <Text>Phone:</Text>
                                    <Text className={ "break-words" }>{ orgaData.contact.phoneNumber }</Text>
                                </>
                            }
                        </div>
                        <Separator className={ "my-4" }/>
                    </div>
                }
                <div className={ "space-y-6" }>
                    <Text asTag={ "h3" } className={ "text-muted-foreground" } styleVariant={ "h6" }>
                        Owners
                    </Text>
                    {
                        orgaData?.owners && orgaData?.owners?.length > 0 &&
                        orgaData.owners?.map( ( owner ) => {
                            return (
                                <NavLink to={ `/profile/${ owner.id }` } key={ owner.id }>
                                    <div className={ "flex flex-col max-w-max justify-center items-center" }
                                    >
                                        <Avatar>
                                            <AvatarImage src={ owner.avatarUrl }/>
                                            <AvatarFallback>
                                                <User/>
                                            </AvatarFallback>
                                        </Avatar>
                                        <Text styleVariant={ "smallMuted" }>{ owner.name }</Text>
                                    </div>
                                </NavLink>
                            )
                        } )
                    }
                </div>
            </CardContent>
        </Card>
    )
}
