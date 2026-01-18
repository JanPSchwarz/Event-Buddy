import PageWrapper from "@/components/PageWrapper.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { Building2, Plus } from "lucide-react";
import { useGetAllOrganizations } from "@/api/generated/organization/organization.ts";
import CustomLoader from "@/components/CustomLoader.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useNavigate } from "react-router";
import OrganizationCard from "@/components/organization/OrganizationCard.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";

export default function OrganizationsPage() {

    const { data: allOrganization, isLoading } = useGetAllOrganizations();

    const { user } = useContextUser();

    const navigate = useNavigate();

    if ( isLoading ) {
        return (
            <CustomLoader size={ "size-6" } text={ "Loading..." }/>
        );
    }

    const handleCreateOrganization = () => {
        if ( !user?.id ) {
            toast.error( "You must be logged in!" );
        } else {
            navigate( "/organization/create" );
        }
    }

    return (
        <PageWrapper className={ "mb-12" }>
            <div className={ "w-full space-y-4 flex flex-col" }>
                <div className={ "w-full flex justify-center items-center" }>
                    <MainHeading heading={ "Organizations" } subheading={ "Find an Organization" }
                                 Icon={ Building2 }/>
                </div>
                <Button size={ "sm" } className={ "ml-auto" } onClick={ handleCreateOrganization }>
                    <Plus/>
                    Create Organization
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
