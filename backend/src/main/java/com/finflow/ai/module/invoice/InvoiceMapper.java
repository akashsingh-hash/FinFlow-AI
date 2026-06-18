package com.finflow.ai.module.invoice;

import com.finflow.ai.module.invoice.dto.InvoiceRequest;
import com.finflow.ai.module.invoice.dto.InvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.name")
    InvoiceResponse toResponse(Invoice invoice);

    Invoice toEntity(InvoiceRequest request);

    void updateEntityFromRequest(InvoiceRequest request, @MappingTarget Invoice invoice);
}
