import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { Card, CardAction, CardContent } from "@/components/ui/card.tsx";
import { useGetImageAsDataUrl } from "@/api/generated/image-controller/image-controller.ts";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar.tsx";
import { User } from "lucide-react";
import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";

type OrganizationCardProps = {
    orgData: OrganizationResponseDto
}

export default function OrganizationCard( { orgData }: Readonly<OrganizationCardProps> ) {

    const { data: imageData } = useGetImageAsDataUrl( orgData.imageId || "", {
        query: { enabled: !!orgData.imageId },
    } )

    return (
        <Card className={ "py-3 w-[350px]" }>
            <CardContent>
                <div className={ "flex justify-between gap-4 items-start" }>
                    <div>
                        <Text styleVariant={ "h6" }>
                            { orgData.name }
                        </Text>
                        <Text styleVariant={ "smallMuted" }>
                            { orgData.location?.city }
                        </Text>
                        <CardAction className={ "mt-4 mr-auto" }>
                            <Button size={ "sm" } asChild>
                                <NavLink to={ `/organization/${ orgData.slug }` }>
                                    View Organization
                                </NavLink>
                            </Button>
                        </CardAction>
                    </div>
                    <Avatar className={ "size-16" }>
                        <AvatarImage src={ imageData?.data }/>
                        <AvatarFallback>
                            <User/>
                        </AvatarFallback>
                    </Avatar>
                </div>
            </CardContent>
        </Card>
    )
}
