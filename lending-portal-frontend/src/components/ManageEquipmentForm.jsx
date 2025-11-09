const ManageEquipmentForm = ({ formState, onChange, onSubmit }) => {
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="cardy">
      <h2>Add equipment</h2>
      <form onSubmit={onSubmit}>
        <label>Name</label>
        <input value={formState.itemName} onChange={updateField("itemName")} required />
        <label>Category</label>
        <input value={formState.category} onChange={updateField("category")} required />
        <label>Condition Notes</label>
        <textarea rows={3} value={formState.conditionNote} onChange={updateField("conditionNote")} />
        <label>Total Quantity</label>
        <input type="number" value={formState.totalQuantity} min="1" onChange={updateField("totalQuantity")} />
        <label>Available Now</label>
        <input value={formState.availableQuantity ?? ""} type="number" onChange={updateField("availableQuantity")} />
        <button type="submit" className="btn btnPrimary btnFull">
          Save Item
        </button>
      </form>
    </div>
  );
};

export default ManageEquipmentForm;
