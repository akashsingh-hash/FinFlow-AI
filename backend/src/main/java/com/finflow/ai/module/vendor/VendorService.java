package com.finflow.ai.module.vendor;

import com.finflow.ai.module.vendor.dto.VendorRequest;
import com.finflow.ai.module.vendor.dto.VendorResponse;

import java.util.List;

public interface VendorService {
    VendorResponse addVendor(VendorRequest request, Long companyId);
    VendorResponse updateVendor(Long id, VendorRequest request);
    void deleteVendor(Long id);
    List<VendorResponse> getCompanyVendors(Long companyId);
    VendorResponse getVendorById(Long id);
}
