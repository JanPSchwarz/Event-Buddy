import PageWrapper from "@/components/PageWrapper.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { Building2, Plus } from "lucide-react";
import { useGetAllOrganizations } from "@/api/generated/organization/organization.ts";
import CustomLoader from "@/components/CustomLoader.tsx";
import { Button } from "@/components/ui/button.tsx";
import { NavLink } from "react-router";
import OrganizationCard from "@/components/organization/OrganizationCard.tsx";

export default function OrganizationsPage() {

    const { data: allOrganization, isLoading } = useGetAllOrganizations();

    if ( isLoading ) {
        return (
            <CustomLoader size={ "size-6" } text={ "Loading..." }/>
        );
    }

    return (
        <PageWrapper className={ "mb-12" }>
            <div className={ "w-full space-y-4 flex flex-col" }>
                <div className={ "w-full flex justify-center items-center" }>
                    <MainHeading heading={ "All Organizations" } subheading={ "Find an Organization" }
                                 Icon={ Building2 }/>
                </div>
                <Button asChild size={ "sm" } className={ "ml-auto" }>
                    <NavLink to={ "/organization/create" }>
                        <Plus/>
                        Create Organization
                    </NavLink>
                </Button>
            </div>
            <div
                className={ "grid grid-cols-[repeat(auto-fit,minmax(350px,1fr))] place-items-center items-start justify-center flex-wrap gap-12 w-full" }>
                {
                    allOrganization?.data.map( ( organization ) => {
                            return (
                                <OrganizationCard key={ organization.id } orgData={ organization }/>
                            )
                        }
                    )
                }
                {
                    ( !allOrganization || allOrganization.data.length === 0 ) &&
                    <p>No organizations found.</p>
                }
            </div>
            <div>

            </div>
        </PageWrapper>
    )
}
