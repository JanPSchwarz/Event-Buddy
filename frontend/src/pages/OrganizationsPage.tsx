import PageWrapper from "@/components/shared/PageWrapper.tsx";
import MainHeading from "@/components/shared/MainHeading.tsx";
import { Building2, Plus } from "lucide-react";
import { useGetAllOrganizations } from "@/api/generated/organization/organization.ts";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import { Button } from "@/components/ui/button.tsx";
import { useNavigate, useSearchParams } from "react-router";
import OrganizationCard from "@/components/organization/OrganizationCard.tsx";
import { useContextUser } from "@/context/UserProvider.tsx";
import { toast } from "sonner";

export default function OrganizationsPage() {

    const { data: allOrganization, isLoading } = useGetAllOrganizations();

    const { user } = useContextUser();

    const navigate = useNavigate();

    const [ _, setSearchParams ] = useSearchParams();

    if ( isLoading ) {
        return (
            <CustomLoader size={ "size-6" } text={ "Loading..." }/>
        );
    }

    const handleCreateOrganization = () => {
        if ( Object.keys( user ).length > 0 ) {
            navigate( "/organization/create" );
        } else {
            toast.error( "You must be logged in!" );
            setSearchParams( ( searchParams ) => {
                searchParams.set( "loginModal", "true" );
                return searchParams;
            } );
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
