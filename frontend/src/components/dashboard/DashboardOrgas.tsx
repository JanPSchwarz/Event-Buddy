import Text from "@/components/typography/Text.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";
import type { OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import OrganizationCard from "@/components/organization/OrganizationCard.tsx";
import { Plus } from "lucide-react";

type DashboardOrgaProps = {
    organizations: OrganizationResponseDto[] | undefined,
    isLoading?: boolean,

}

export default function DashboardOrgas( { organizations = [], isLoading }: Readonly<DashboardOrgaProps> ) {

    return (
        <div className={ "space-y-12 mb-12" }>
            <div className={ "flex" }>
                <Button size={ "sm" } className={ "ml-auto" } asChild>
                    <NavLink to={ "/organization/create" }>
                        <Plus/> Create Organization
                    </NavLink>
                </Button>
            </div>
            {
                isLoading &&
                <CustomLoader/>
            }
            {
                !isLoading && organizations.length === 0 ?
                    <Text className={ "text-center" }>
                        No Organization here yet.
                    </Text>
                    :
                    <div className={ "flex gap-6 justify-start gap-8 flex-wrap" }>
                        { organizations.map( ( orga ) => (
                            <OrganizationCard key={ orga.id } orgData={ orga }/>
                        ) ) }
                    </div>
            }
        </div>
    )
}
