import OrganizationForm from "@/components/organization/OrganizationForm.tsx";
import type { OrganizationRequestDto, OrganizationResponseDto } from "@/api/generated/openAPIDefinition.schemas.ts";
import { useUpdateOrganization } from "@/api/generated/organization/organization.ts";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import CustomLoader from "@/components/shared/CustomLoader.tsx";

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
                    queryClient.invalidateQueries().then( () => {
                            toast.success( "Organization updated successfully!" );
                            if ( response.data.name === organizationData.name ) {
                                closeEdit();
                            } else {
                                navigate( `/organization/${ response.data.slug }` );
                            }
                        }
                    );
                },
                onError: ( error ) => {
                    console.error( "Error updating organization:", error );
                    toast.error( error.response?.data.error || "Error updating" );
                },
            }
        );

    }

    const isSubmitting = updateOrga.isPending;

    return (
        <div className={ "w-full max-w-[800px] relative mb-12" }>
            {
                isSubmitting &&
                <CustomLoader size={ "size-8" } text={ "Submitting..." }
                              className={ "absolute top-1/2 -translate-y-1/2" }/>
            }
            <OrganizationForm onSubmit={ handleSubmit }
                              organizationData={ organizationData }
                              isLoading={ isSubmitting }
                              formClassName={ `${ isSubmitting && "opacity-50" }` }
            />
        </div>
    )
}
