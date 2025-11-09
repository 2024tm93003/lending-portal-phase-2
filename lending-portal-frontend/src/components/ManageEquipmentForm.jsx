/**
 * ManageEquipmentForm
 *
 * Simple form used by administrators to add or edit equipment items.
 * Controlled by `formState` and emits updates via `onChange`.
 *
 * Props:
 * @param {Object} formState - Equipment form values: { itemName, category, conditionNote, totalQuantity, availableQuantity }.
 * @param {function(Object):void} onChange - Called with updated form state when a field changes.
 * @param {function(Event):void} onSubmit - Form submit handler.
 */
const ManageEquipmentForm = ({ formState, onChange, onSubmit, formErrors = {} }) => {
  /**
   * Create a change handler for a specific form field.
   * @param {string} field - Field to update.
   * @returns {function(Event):void}
   */
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-xl font-semibold">Add equipment</h2>
      <form onSubmit={onSubmit} className="mt-4 space-y-3">
        <div>
          <label className="block text-sm font-medium text-slate-700">Name</label>
          <input value={formState.itemName} onChange={updateField("itemName")} required className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
          {formErrors.itemName && <p className="text-xs text-red-600 mt-1">{formErrors.itemName}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700">Category</label>
          <input value={formState.category} onChange={updateField("category")} required className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
          {formErrors.category && <p className="text-xs text-red-600 mt-1">{formErrors.category}</p>}
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700">Condition Notes</label>
          <textarea rows={3} value={formState.conditionNote} onChange={updateField("conditionNote")} className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-700">Total Quantity</label>
            <input type="number" value={formState.totalQuantity} min="1" onChange={updateField("totalQuantity")} className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
            {formErrors.totalQuantity && <p className="text-xs text-red-600 mt-1">{formErrors.totalQuantity}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">Available Now</label>
            <input value={formState.availableQuantity ?? ""} type="number" onChange={updateField("availableQuantity")} className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
            {formErrors.availableQuantity && <p className="text-xs text-red-600 mt-1">{formErrors.availableQuantity}</p>}
          </div>
        </div>
        <div>
          <button type="submit" className="w-full bg-sky-600 hover:bg-sky-700 text-white py-2 rounded-md font-semibold">Save Item</button>
        </div>
      </form>
    </div>
  );
};

export default ManageEquipmentForm;
