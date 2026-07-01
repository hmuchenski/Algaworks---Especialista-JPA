package com.algaworks.ecommerce.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ItemPedidoId implements Serializable {

	private static final long serialVersionUID = 8049491925148270256L;

	@Column(name = "pedido_id")
	private Integer pedidoId;

	@Column(name = "produto_id")
	private Integer produtoId;

	public ItemPedidoId() {
		super();
	}

	public ItemPedidoId(Integer pedidoId, Integer produtoId) {
		super();
		this.pedidoId = pedidoId;
		this.produtoId = produtoId;
	}

	public Integer getPedidoId() {
		return pedidoId;
	}

	public void setPedidoId(Integer pedidoId) {
		this.pedidoId = pedidoId;
	}

	public Integer getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Integer produtoId) {
		this.produtoId = produtoId;
	}

}
