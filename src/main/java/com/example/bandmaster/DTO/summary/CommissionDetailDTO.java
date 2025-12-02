package com.example.bandmaster.DTO.summary;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CommissionDetailDTO(
        @JsonProperty("vendedor_id") Long vendedorId,
        @JsonProperty("vendedor_nome") String vendedorNome,
        @JsonProperty("comissao_id") Long comissaoId,
        @JsonProperty("cod_produto") Long codProduto,
        @JsonProperty("produto") String produtoDescricao,
        @JsonProperty("venda") Long vendaId,
        @JsonProperty("data_venda") LocalDateTime dataVenda,
        @JsonProperty("parcela_cod") Long parcelaCod,

        @JsonProperty("valor_venda") BigDecimal valorVenda,
        @JsonProperty("valor_recebido") BigDecimal valorRecebido,
        @JsonProperty("valor_estornado") BigDecimal valorEstornado,
        @JsonProperty("valor_comissao_total") BigDecimal valorComissaoTotal,
        @JsonProperty("valor_comissao_paga") BigDecimal valorComissaoPaga,

        @JsonProperty("data_limite") String dataLimite // String para controlar nulo/formato
) {}