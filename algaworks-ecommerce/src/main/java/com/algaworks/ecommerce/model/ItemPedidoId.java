package com.algaworks.ecommerce.model;

import java.io.Serializable;

public class ItemPedidoId implements Serializable {

	private static final long serialVersionUID = 8049491925148270256L;

	private Integer pedidoId;

	private Integer produtoId;

	public ItemPedidoId() {
		super();
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
