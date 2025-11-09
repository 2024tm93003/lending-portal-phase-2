const BorrowRequestForm = ({ items, formState, onChange, onSubmit }) => {
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="cardy">
      <h2>Borrow something</h2>
      <form onSubmit={onSubmit}>
        <label>Select item</label>
        <select value={formState.equipmentId} onChange={updateField("equipmentId")} required>
          <option value="">-- Choose --</option>
          {items.map((equip) => (
            <option key={equip.id} value={equip.id}>
              {equip.itemName} ({equip.availableQuantity} free)
            </option>
          ))}
        </select>
        <label>Start Date</label>
        <input type="date" value={formState.startDate} onChange={updateField("startDate")} required />
        <label>End Date</label>
        <input type="date" value={formState.endDate} onChange={updateField("endDate")} required />
        <label>Quantity</label>
        <input type="number" min="1" value={formState.qty} onChange={updateField("qty")} />
        <button type="submit" className="btn btnPrimary btnFull">
          Submit Request
        </button>
      </form>
    </div>
  );
};

export default BorrowRequestForm;
