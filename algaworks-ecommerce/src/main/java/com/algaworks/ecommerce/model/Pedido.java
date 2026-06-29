package com.algaworks.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.algaworks.ecommerce.listener.GerarLogListener;
import com.algaworks.ecommerce.listener.GerarNotaFiscalListener;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@EntityListeners(value = { GerarNotaFiscalListener.class, GerarLogListener.class })
@Entity
@Table(name = "pedido")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "data_criacao", updatable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_ultima_atualizacao", insertable = false)
	private LocalDateTime dataUltimaAtualizacao;

	@Column(name = "data_conclusao")
	private LocalDateTime dataConclusao;

	private BigDecimal total;

	@Enumerated(EnumType.STRING)
	private StatusPedido status;

	@Embedded
	private EnderecoEntregaPedido enderecoEntrega;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;

	@OneToMany(mappedBy = "pedido")
	private List<ItemPedido> itensPedidos;

	@OneToOne(mappedBy = "pedido")
	private PagamentoCartao pagamento;

	@OneToOne(mappedBy = "pedido")
	private NotaFiscal notaFiscal;

	public Pedido() {
		super();
	}

	public Pedido(Integer id, LocalDateTime dataCriacao, LocalDateTime dataConclusao, BigDecimal total,
			StatusPedido status, EnderecoEntregaPedido enderecoEntrega, Cliente cliente, List<ItemPedido> itensPedidos,
			PagamentoCartao pagamento, NotaFiscal notaFiscal, LocalDateTime dataUltimaAtualizacao) {
		super();
		this.id = id;
		this.dataCriacao = dataCriacao;
		this.dataConclusao = dataConclusao;
		this.total = total;
		this.status = status;
		this.enderecoEntrega = enderecoEntrega;
		this.cliente = cliente;
		this.itensPedidos = itensPedidos;
		this.pagamento = pagamento;
		this.notaFiscal = notaFiscal;
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	
	public boolean isPago()
	{
		return StatusPedido.PAGO.equals(status);
	}

//  @PrePersist
//  @PreUpdate
	public void calcularTotal() {
		if (itensPedidos != null) {
			total = itensPedidos.stream().map(ItemPedido::getPrecoProduto).reduce(BigDecimal.ZERO, BigDecimal::add);
		}
	}

	@PrePersist
	public void aoPersistir() {
		System.out.println("Antes de persistir Pedido.");
		dataCriacao = LocalDateTime.now();
		calcularTotal();
	}

	@PostPersist
	public void aposPersistir() {
		System.out.println("Após persistir Pedido.");
	}

	@PreUpdate
	public void aoAtualizar() {
		System.out.println("Antes de atualizar Pedido.");
		dataUltimaAtualizacao = LocalDateTime.now();
		calcularTotal();
	}

	@PostUpdate
	public void aposAtualizar() {
		System.out.println("Após atualizar Pedido.");
	}

	@PreRemove
	public void aoRemover() {
		System.out.println("Antes de remover Pedido.");
	}

	@PostRemove
	public void aposRemover() {
		System.out.println("Após remover Pedido.");
	}

	@PostLoad
	public void aoCarregar() {
		System.out.println("Após carregar o Pedido.");
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public LocalDateTime getDataConclusao() {
		return dataConclusao;
	}

	public void setDataConclusao(LocalDateTime dataConclusao) {
		this.dataConclusao = dataConclusao;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public StatusPedido getStatus() {
		return status;
	}

	public void setStatus(StatusPedido status) {
		this.status = status;
	}

	public EnderecoEntregaPedido getEnderecoEntrega() {
		return enderecoEntrega;
	}

	public void setEnderecoEntrega(EnderecoEntregaPedido enderecoEntrega) {
		this.enderecoEntrega = enderecoEntrega;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<ItemPedido> getItensPedidos() {
		return itensPedidos;
	}

	public void setItensPedidos(List<ItemPedido> itensPedidos) {
		this.itensPedidos = itensPedidos;
	}

	public PagamentoCartao getPagamento() {
		return pagamento;
	}

	public void setPagamento(PagamentoCartao pagamento) {
		this.pagamento = pagamento;
	}

	public NotaFiscal getNotaFiscal() {
		return notaFiscal;
	}

	public void setNotaFiscal(NotaFiscal notaFiscal) {
		this.notaFiscal = notaFiscal;
	}

	public LocalDateTime getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}

	public void setDataUltimaAtualizacao(LocalDateTime dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pedido other = (Pedido) obj;
		return Objects.equals(id, other.id);
	}

}
