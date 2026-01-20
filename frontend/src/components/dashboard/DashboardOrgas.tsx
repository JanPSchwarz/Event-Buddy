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
        <div className={ "space-y-4 mb-12" }>
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
                    <div className={ "flex justify-center md:justify-start gap-8 flex-wrap" }>
                        { organizations.map( ( orga ) => (
                            <div key={ orga.id } className={ "border p-4 rounded-md space-y-4" }>
                                <OrganizationCard orgData={ orga }/>
                                <Text styleVariant={ "smallMuted" }>
                                    Orga Id: { orga.id }
                                </Text>
                                <div className={ "flex justify-between" }>
                                    <Button asChild>
                                        <NavLink to={ `/organization/${ orga.slug }` }>
                                            Manage
                                        </NavLink>
                                    </Button>
                                    <Button asChild variant={ "secondary" }>
                                        <NavLink to={ `/organization/${ orga.slug }?edit=true` }>
                                            Edit
                                        </NavLink>
                                    </Button>
                                </div>
                            </div>
                        ) ) }
                    </div>
            }
        </div>
    )
}
