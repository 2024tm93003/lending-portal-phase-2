/**
 * RequestTable
 *
 * Displays a table of borrow requests. Actions available depend on the `role` prop.
 *
 * Props:
 * @param {Array<Object>} requests - Array of request objects to render. Each request should have { id, gear, requester, startDate, endDate, qty, status }.
 * @param {string} role - Current user's role (e.g. 'USER', 'STAFF', 'ADMIN'). Manager roles see action buttons.
 * @param {function(number, string, string=):void} onDecision - Callback invoked when an action is taken. Signature: (requestId, action, [reason]).
 */
const RequestTable = ({ requests, role, onDecision }) => {
  // Determine whether the current user should see managerial actions
  const isManager = role === "STAFF" || role === "ADMIN";
  const emptyColSpan = isManager ? 7 : 6;

  const statusClass = (status) => {
    switch (status) {
      case "PENDING":
        return "bg-amber-100 text-amber-800";
      case "APPROVED":
        return "bg-emerald-100 text-emerald-800";
      case "ISSUED":
        return "bg-sky-100 text-sky-800";
      case "REJECTED":
        return "bg-rose-100 text-rose-800";
      case "RETURNED":
        return "bg-violet-100 text-violet-800";
      default:
        return "bg-slate-100 text-slate-800";
    }
  };

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-xl font-semibold">Borrow Requests</h2>
      <div className="mt-4 overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">ID</th>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Item</th>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Borrower</th>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Duration</th>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Qty</th>
              <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Status</th>
              {isManager && <th className="px-3 py-2 text-left text-sm font-medium text-slate-600">Actions</th>}
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-slate-100">
            {requests.map((request) => (
              <tr key={request.id}>
                <td className="px-3 py-2 text-sm text-slate-700">{request.id}</td>
                <td className="px-3 py-2 text-sm text-slate-700">{request.gear?.itemName || "-"}</td>
                <td className="px-3 py-2 text-sm text-slate-700">{request.requester?.displayName || request.requester?.username}</td>
                <td className="px-3 py-2 text-sm text-slate-700">{request.startDate} → {request.endDate}</td>
                <td className="px-3 py-2 text-sm text-slate-700">{request.qty}</td>
                <td className="px-3 py-2">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${statusClass(request.status)}`}>{request.status}</span>
                </td>
                {isManager && (
                  <td className="px-3 py-2 space-x-2">
                    {request.status === "PENDING" && (
                      <>
                        <button className="px-3 py-1 rounded-md bg-sky-600 text-white text-sm" onClick={() => onDecision(request.id, "approve")}>
                          Approve
                        </button>
                        <button className="px-3 py-1 rounded-md bg-rose-600 text-white text-sm" onClick={() => onDecision(request.id, "reject", "not available")}>
                          Reject
                        </button>
                      </>
                    )}
                    {request.status === "APPROVED" && (
                      <button className="px-3 py-1 rounded-md bg-sky-600 text-white text-sm" onClick={() => onDecision(request.id, "issue")}>
                        Mark Issued
                      </button>
                    )}
                    {request.status === "ISSUED" && (
                      <button className="px-3 py-1 rounded-md bg-sky-600 text-white text-sm" onClick={() => onDecision(request.id, "return")}>
                        Close Return
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {requests.length === 0 && (
              <tr>
                <td colSpan={emptyColSpan} className="px-3 py-6 text-center text-slate-600">No requests found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default RequestTable;
