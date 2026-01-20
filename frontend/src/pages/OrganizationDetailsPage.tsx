import { useParams } from "react-router";
import { useContextUser } from "@/context/UserProvider.tsx";
import CustomLoader from "@/components/shared/CustomLoader.tsx";
import OrganizationView from "@/components/organization/OrganizationView.tsx";
import PageWrapper from "@/components/shared/PageWrapper.tsx";
import { useGetOrganizationBySlug } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import Text from "@/components/typography/Text.tsx";
import PageNotFound from "@/pages/PageNotFound.tsx";
import OrganizationOwnerView from "@/components/organization/OrganizationOwnerView.tsx";

export default function OrganizationDetailsPage() {
    const { orgaSlug } = useParams();

    const { user: loggedInUser, isLoading: isLoadingLoggedInUser } = useContextUser();

    const { data: organizationData, isLoading: isLoadingOrganization } = useGetOrganizationBySlug( orgaSlug ?? "" );

    if ( isLoadingLoggedInUser || isLoadingOrganization ) {
        return <CustomLoader size={ "size-6" } text={ "Loading..." }/>
    }

    const isOwner = loggedInUser.organizations?.includes( organizationData?.data.id || "" );

    if ( !organizationData ) {
        toast.error( "Organization Data not found" );
        return (
            <div className={ "w-full flex flex-col justify-center items-center gap-12" }>
                <Text asTag={ "h1" } styleVariant={ "h3" }
                      className={ "text-primary border-b border-muted-foreground" }>The Organization you are looking for
                    does not exist</Text>
                <PageNotFound/>
            </div>
        )
    }

    return (
        <PageWrapper>
            { isOwner ?
                <OrganizationOwnerView orgData={ organizationData?.data }/>
                :
                <OrganizationView orgaData={ organizationData.data }/>
            }
        </PageWrapper>
    )
}
