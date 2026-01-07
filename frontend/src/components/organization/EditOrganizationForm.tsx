import OrganizationForm from "@/components/organization/OrganizationForm.tsx";
import type { OrganizationRequestDto, OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { getGetOrganizationBySlugQueryKey, useUpdateOrganization } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router";

type EditOrganizationFormProps = {
    organizationData: OrganizationResponseDto,
    closeEdit: () => void,
}

export default function EditOrganizationForm( { organizationData, closeEdit }: Readonly<EditOrganizationFormProps> ) {

    const navigate = useNavigate();

    const updateOrga = useUpdateOrganization(
        {
            axios: {
                withCredentials: true,
            }
        }
    )

    const queryClient = useQueryClient();

    const handleSubmit = ( organizationFormData: OrganizationRequestDto, imageFile: File | null, deleteImage: boolean | undefined ) => {


        if ( !organizationData.id ) {
            console.error( "Organization ID is missing. Cannot update organization." );
            return;
        }

        updateOrga.mutate(
            {
                data: {
                    updateOrganization: organizationFormData,
                    image: imageFile || undefined,
                    deleteImage
                },
                organizationId: organizationData.id,
            },
            {
                onSuccess: ( response ) => {
                    console.log( "Organization updated successfully:", response );

                    toast.success( "Organization updated successfully!" );

                    if ( response.data.name === organizationData.name ) {
                        queryClient.invalidateQueries( { queryKey: getGetOrganizationBySlugQueryKey( organizationData.slug ) } )
                        closeEdit();
                    } else {
                        navigate( `/organization/${ response.data.slug }` );
                    }
                },
                onError: ( error ) => {
                    console.error( "Error updating organization:", error );
                    toast.error( error.response?.data.error || "Error updating" );
                }
            }
        );

    }

    return (
        <div className={ "w-full max-w-[800px] mb-12" }>
            <OrganizationForm onSubmit={ handleSubmit } organizationData={ organizationData }/>
        </div>
    )
}
