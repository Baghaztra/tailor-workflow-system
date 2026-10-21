export const formatDate = (value: string) => new Date(value).toLocaleString()

export const formatPrice = (value: number | null) =>
  value == null
    ? '—'
    : new Intl.NumberFormat('id-ID', {
        style: 'currency',
        currency: 'IDR',
        maximumFractionDigits: 0,
      }).format(value)
