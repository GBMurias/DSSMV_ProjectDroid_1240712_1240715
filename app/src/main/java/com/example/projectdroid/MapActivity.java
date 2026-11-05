package com.example.projectdroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

import model.Parts;
import model.Vehicle;
import controller.MapController;
import model.Slot;

public class MapActivity extends AppCompatActivity {

    // ==========================================
    // 1. VARIÁVEIS
    // ==========================================

    // --- Estado / dados ---
    private List<Slot> globalSlotsList;
    private String selectedSlotId = "";
    private String currentClickedSlotId = "";   // slot clicado no modo "consulta"
    private String vehicleModel = "";
    private String origin;
    private boolean intakeRegistered = false;   // true depois de o intake ter ocupado um slot

    // --- Popup de confirmação (Intake) / slot vazio ---
    private CardView cardPopup;
    private TextView tvVehicleInfo;
    private TextView txtMapQuestion;
    private Button btnNo;
    private Button btnYes;

    // --- Popup de opções do slot ---
    private CardView cardParkingOptions;
    private TextView txtVehicleDescription;
    private TextView txtVehicleVin;
    private TextView btnEmptySpot;
    private TextView btnRemovePieces;
    private TextView btnBackParkingOpt;

    private Vehicle currentSelectedVehicle;

    // --- Popup de peças ---
    private CardView cardPartsPopup;
    private TextView btnClosePartsPopup;
    private ListView listViewParts;
    private Button btnCancelParts;
    private Button btnAddPart;

    // --- Botão "Voltar ao menu" (criado por código, só depois de registar o intake) ---
    private Button btnBackToMain;

    // ==========================================
    // 2. CICLO DE VIDA
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        readIntentExtras();
        setupSlotClickListeners();

        // Popup de confirmação (Intake)
        setupBtnYes();
        setupBtnNo();

        // Popup de opções do slot
        setupBtnEmptySpot();
        setupBtnRemovePieces();
        setupBtnBackParkingOpt();

        // Popup de peças
        setupBtnClosePartsPopup();
        setupBtnCancelParts();
        setupBtnAddPart();

        loadMapData();
    }

    // ==========================================
    // 3. INICIALIZAÇÃO (chamada pelo onCreate)
    // ==========================================

    private void initViews() {
        cardPopup = findViewById(R.id.mapcardPopup);
        tvVehicleInfo = findViewById(R.id.mapVehicleInfo);
        txtMapQuestion = findViewById(R.id.mapQuestionText);
        btnNo = findViewById(R.id.mapBtnNo);
        btnYes = findViewById(R.id.mapBtnYes);

        cardParkingOptions = findViewById(R.id.card_parking_options);
        txtVehicleDescription = findViewById(R.id.txt_vehicle_description);
        txtVehicleVin = findViewById(R.id.txt_vehicle_vin);
        btnEmptySpot = findViewById(R.id.btn_empty_spot);
        btnRemovePieces = findViewById(R.id.btn_remove_pieces);
        btnBackParkingOpt = findViewById(R.id.btn_back_parking_opt);

        cardPartsPopup = findViewById(R.id.MapcardPartsPopup);
        btnClosePartsPopup = findViewById(R.id.btnClosePartsPopup);
        listViewParts = findViewById(R.id.listViewParts);
        btnCancelParts = findViewById(R.id.btnCancelParts);
        btnAddPart = findViewById(R.id.btnAddPart);
    }

    private void readIntentExtras() {
        if (getIntent() != null) {
            origin = getIntent().getStringExtra("Where");
            vehicleModel = getIntent().getStringExtra("Modelo");
        }
    }

    private void setupSlotClickListeners() {
        GridLayout gridLayout = findViewById(R.id.grid_layout);

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);

            if (child instanceof FrameLayout) {
                FrameLayout tempslot = (FrameLayout) child;
                final String slotIdName = getResources().getResourceEntryName(tempslot.getId());

                tempslot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSlotClicked(slotIdName);
                    }
                });
            }
        }
    }

    // ==========================================
    // 4. EVENT HANDLERS (só chamam funções)
    // ==========================================

    // ------------------ Clique num slot do mapa ------------------

    private void onSlotClicked(String slotIdName) {
        Slot clickedSlot = findSlotById(slotIdName);

        if ("IntakeAct".equals(origin)) {
            // O intake já foi registado: não deixa ocupar mais slots com o mesmo veículo
            if (intakeRegistered) {
                showToast("Intake já registado. Usa o botão para voltar ao menu.");
                return;
            }

            if (clickedSlot != null && clickedSlot.isOccupied()) {
                showIntakeOccupiedPopup(slotIdName);
                return;
            }
            showIntakeConfirmPopup(slotIdName);
        } else {
            // Guarda o slot clicado para o botão "Remove Parts" poder usá-lo
            currentClickedSlotId = slotIdName;
            popUpInformationSlot(slotIdName);
            // popUpMeuParts já NÃO é chamado aqui; é chamado pelo btnRemovePieces
        }
    }

    // ------------------ Popup de confirmação (Intake) ------------------

    private void setupBtnYes() {
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmIntakeClicked();
            }
        });
    }

    private void setupBtnNo() {
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPopup.setVisibility(View.GONE);
            }
        });
    }

    // ------------------ Popup de opções do slot ------------------

    private void setupBtnEmptySpot() {
        btnEmptySpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEmptySpotClicked();
            }
        });
    }

    private void setupBtnRemovePieces() {
        btnRemovePieces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpMeuParts(currentClickedSlotId);
            }
        });
    }

    private void setupBtnBackParkingOpt() {
        btnBackParkingOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardParkingOptions.setVisibility(View.GONE);
            }
        });
    }

    // ------------------ Popup de peças ------------------

    private void setupBtnClosePartsPopup() {
        btnClosePartsPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPartsPopup.setVisibility(View.GONE);
            }
        });
    }

    private void setupBtnCancelParts() {
        btnCancelParts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listViewParts.clearChoices();
                cardPartsPopup.setVisibility(View.GONE);
            }
        });
    }

    private void setupBtnAddPart() {
        btnAddPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPartsClicked();
            }
        });
    }

    // ==========================================
    // 5. LÓGICA DOS HANDLERS
    // ==========================================

    // ------------------ Intake: confirmar slot ------------------

    private void onConfirmIntakeClicked() {
        if (selectedSlotId == null || selectedSlotId.isEmpty()) {
            return;
        }

        String columnStr = selectedSlotId.substring(0, 1);
        int rowInt = Integer.parseInt(selectedSlotId.substring(1));
        String vin = getIntent().getStringExtra("VIN");

        // Validação feita ANTES de lançar a thread (Toast não pode ser chamado fora da UI thread)
        if (vin == null || vin.isEmpty()) {
            showToast("Erro: O VIN do veículo não foi recebido!");
            return;
        }

        occupySlotInBackground(new Slot(rowInt, columnStr, vin));
    }

    private void occupySlotInBackground(final Slot slot) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MapController ctrl = new MapController();
                final boolean sucess = ctrl.registerVIN2Slot(MapActivity.this, slot);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (sucess) {
                            showToast("Lugar " + selectedSlotId + " ocupado com sucesso!");
                            onIntakeSlotRegistered();
                        } else {
                            showToast("Erro ao atualizar o slot na base de dados.");
                        }
                        cardPopup.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    /** Chamado na UI thread quando o intake acabou de ocupar um slot: recarrega o mapa e mostra o botão de voltar. */
    private void onIntakeSlotRegistered() {
        intakeRegistered = true;
        loadMapData();          // volta a carregar o mapa, já com o veículo no slot
        showBackToMainButton();
    }

    // ------------------ Esvaziar slot (enviar veículo para history) ------------------

    private void onEmptySpotClicked() {
        Slot slotToEmpty = findSlotById(currentClickedSlotId);

        if (slotToEmpty == null || !slotToEmpty.isOccupied()) {
            showToast("Slot not found or already empty.");
            return;
        }

        String vin = slotToEmpty.getCurrent_vin();
        if (vin == null || vin.isEmpty()) {
            showToast("Erro: Este slot não tem um VIN associado.");
            return;
        }

        checkPartsBeforeEmptyingInBackground(slotToEmpty, vin);
    }

    /**
     * Vai buscar o veículo (fora da UI thread) para saber se ainda tem peças:
     * - sem peças  -> esvazia o slot logo
     * - com peças  -> pergunta se quer enviar todas as peças para o inventory
     */
    private void checkPartsBeforeEmptyingInBackground(final Slot slot, final String vin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapController controller = new MapController();
                    Vehicle vehicle = controller.getVehicle(MapActivity.this, vin);
                    List<Parts> parts = vehicle.getPartsList();
                    final int partsCount = (parts == null) ? 0 : parts.size();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (partsCount == 0) {
                                cardParkingOptions.setVisibility(View.GONE);
                                sendVehicleToHistoryInBackground(slot, vin);
                            } else {
                                showSendAllPartsDialog(slot, vin, partsCount);
                            }
                        }
                    });

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error checking vehicle parts", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastLong("Erro: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    /** Aviso: o veículo ainda tem peças. Se confirmar, vão todas para o inventory e o slot é esvaziado. */
    private void showSendAllPartsDialog(final Slot slot, final String vin, int partsCount) {
        new AlertDialog.Builder(MapActivity.this)
                .setTitle("Veículo com peças")
                .setMessage("Este veículo ainda tem " + partsCount + " peça(s).\n"
                        + "Queres enviar todas as peças para o inventory e esvaziar o slot?")
                .setCancelable(false)
                .setPositiveButton("Enviar tudo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cardParkingOptions.setVisibility(View.GONE);
                        sendAllPartsAndEmptySlotInBackground(slot, vin);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Não faz nada: o popup de opções do slot continua aberto
                    }
                })
                .show();
    }

    /**
     * 1. passa TODAS as peças para o inventory, 2. deixa o veículo sem peças,
     * 3. envia o veículo para history e esvazia o slot.
     */
    private void sendAllPartsAndEmptySlotInBackground(final Slot slot, final String vin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapController controller = new MapController();
                    Vehicle vehicle = controller.getVehicle(MapActivity.this, vin);

                    List<Parts> toInventory = new ArrayList<>();
                    List<Parts> vehicleParts = vehicle.getPartsList();
                    if (vehicleParts != null) {
                        for (Parts p : vehicleParts) {
                            toInventory.add(new Parts(p.getName(), vehicle.getVin(),
                                    vehicle.getModel(), vehicle.getMake(), vehicle.getYear()));
                        }
                    }

                    if (!toInventory.isEmpty()) {
                        // a) Adicionar ao inventário
                        controller.partsFromMap2Inventory(MapActivity.this, toInventory);

                        // b) Só depois deixar o veículo sem peças
                        controller.updateIntakeParts(MapActivity.this, vin, new ArrayList<Parts>());
                    }

                    // c) Veículo -> history e slot vazio
                    scrapVehicleAndEmptySlot(controller, slot, vin);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onVehicleScrapped(vin);
                        }
                    });

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error sending all parts to inventory", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastLong("Erro: " + e.getMessage());
                            loadMapData();
                        }
                    });
                }
            }
        }).start();
    }

    private void sendVehicleToHistoryInBackground(final Slot slot, final String vin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scrapVehicleAndEmptySlot(new MapController(), slot, vin);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentSelectedVehicle = null;
                            showToast("Veículo (VIN: " + vin + ") enviado para o inventory!");
                            loadMapData();
                        }
                    });

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error sending vehicle to inventory", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastLong("Erro: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Passos partilhados (têm de correr fora da UI thread):
     * 1. vai buscar o veículo, 2. copia-o para history, 3. apaga-o do intake, 4. esvazia o slot.
     */
    private void scrapVehicleAndEmptySlot(MapController controller, Slot slot, String vin) throws Exception {
        Vehicle vehicle = controller.getVehicle(MapActivity.this, vin);
        currentSelectedVehicle = vehicle;

        controller.moveIntake2History(MapActivity.this, currentSelectedVehicle);
        controller.deleteIntake(MapActivity.this, currentSelectedVehicle.getVin());
        slot.setCurrent_vin("");
        boolean ok = controller.registerVIN2Slot(MapActivity.this, slot);
        if (!ok) throw new RuntimeException("Não foi possível esvaziar o slot.");
    }

    // ------------------ Passar peças para o inventário ------------------

    private void onAddPartsClicked() {
        // 1. Ler os nomes das peças selecionadas na ListView
        List<String> selectedNames = getSelectedPartNames();

        if (selectedNames.isEmpty()) {
            showToast("Seleciona pelo menos uma peça.");
            return;
        }

        // 2. Obter o slot atual
        Slot currentSlot = findSlotById(currentClickedSlotId);

        if (currentSlot == null || !currentSlot.isOccupied()) {
            showToast("Slot inválido.");
            return;
        }

        String vin = currentSlot.getCurrent_vin();

        // 3. Se vai remover as últimas peças, pergunta primeiro se quer remover o veículo
        if (willRemoveAllParts(selectedNames)) {
            showScrapConfirmDialog(currentSlot, vin, selectedNames);
            return;
        }

        startMovingParts(currentSlot, vin, selectedNames);
    }

    private void startMovingParts(Slot slot, String vin, List<String> selectedNames) {
        // Primeiro: desaparecem da ListView
        removeNamesFromListView(selectedNames);

        // Depois: base de dados (fora da UI thread)
        movePartsToInventoryInBackground(slot, vin, selectedNames);
    }

    private List<String> getSelectedPartNames() {
        SparseBooleanArray checked = listViewParts.getCheckedItemPositions();
        List<String> selectedNames = new ArrayList<>();
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                selectedNames.add((String) listViewParts.getItemAtPosition(checked.keyAt(i)));
            }
        }
        return selectedNames;
    }

    /** true se as peças selecionadas são todas as que ainda estão na ListView. */
    private boolean willRemoveAllParts(List<String> selectedNames) {
        return selectedNames.size() >= listViewParts.getAdapter().getCount();
    }

    private void removeNamesFromListView(List<String> names) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listViewParts.getAdapter();
        for (String name : names) {
            adapter.remove(name);
        }
        listViewParts.clearChoices();
        adapter.notifyDataSetChanged();
    }

    /**
     * Separa as peças do veículo: as selecionadas vão para toInventory (com os dados do veículo),
     * as restantes ficam em remaining.
     */
    private void splitPartsBySelection(Vehicle vehicle, List<String> selectedNames,
                                       List<Parts> toInventory, List<Parts> remaining) {
        List<String> pending = new ArrayList<>(selectedNames);

        for (Parts p : vehicle.getPartsList()) {
            if (pending.remove(p.getName())) {
                // Preenche os dados associados ao veículo
                toInventory.add(new Parts(p.getName(), vehicle.getVin(),
                        vehicle.getModel(), vehicle.getMake(), vehicle.getYear()));
            } else {
                remaining.add(p);
            }
        }
    }

    private void movePartsToInventoryInBackground(final Slot slot, final String vin, final List<String> selectedNames) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapController controller = new MapController();
                    Vehicle vehicle = controller.getVehicle(MapActivity.this, vin);

                    List<Parts> toInventory = new ArrayList<>();
                    List<Parts> remaining = new ArrayList<>();
                    splitPartsBySelection(vehicle, selectedNames, toInventory, remaining);

                    if (toInventory.isEmpty()) {
                        throw new RuntimeException("Peças não encontradas no veículo.");
                    }

                    // a) Adicionar ao inventário
                    controller.partsFromMap2Inventory(MapActivity.this, toInventory);

                    // b) Só depois remover do veículo
                    controller.updateIntakeParts(MapActivity.this, vin, remaining);

                    // c) Se o veículo ficou sem peças -> remover do slot (já foi confirmado pelo utilizador)
                    if (remaining.isEmpty()) {
                        scrapVehicleAndEmptySlot(controller, slot, vin);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onVehicleScrapped(vin);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Peças adicionadas ao inventário!");
                            }
                        });
                    }

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error moving parts to inventory", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToastLong("Erro: " + e.getMessage());
                            // Recarrega a lista real para repor as peças que falharam
                            popUpMeuParts(currentClickedSlotId);
                        }
                    });
                }
            }
        }).start();
    }

    /** Chamado na UI thread quando o veículo ficou sem peças e o slot foi esvaziado. */
    private void onVehicleScrapped(String vin) {
        currentSelectedVehicle = null;
        cardPartsPopup.setVisibility(View.GONE);
        showToastLong("Veículo (VIN: " + vin + ") removido do slot e enviado para o inventory!");
        loadMapData();
    }

    /** Cancelou o abate: nada foi removido, volta ao menu com Empty Slot / Remove Pieces. */
    private void onScrapCancelled() {
        listViewParts.clearChoices();
        cardPartsPopup.setVisibility(View.GONE);
        cardParkingOptions.setVisibility(View.VISIBLE);
    }

    // ==========================================
    // 6. CARREGAMENTO DE DADOS E ATUALIZAÇÃO DA UI
    // ==========================================

    private void loadMapData() {
        new Thread(() -> {
            MapController controller = new MapController();

            try {
                List<Slot> slots = controller.processMapSlots(MapActivity.this);
                globalSlotsList = slots;

                runOnUiThread(() -> {
                    updateMapUI(slots);
                });

            } catch (Exception e) {
                String errorMsg = e.getMessage();
                runOnUiThread(() -> {
                    showToastLong("Error while loading map data: " + errorMsg);
                });
            }
        }).start();
    }

    private void updateMapUI(List<Slot> slots) {
        for (Slot slot : slots) {
            String slotIdString = slot.getSlotID();
            Log.d("PARKING_DEBUG", "Slot: " + slotIdString + " | VIN: " + slot.getCurrent_vin() + " | Occupied: " + slot.isOccupied());
            int frameResId = getResources().getIdentifier(slotIdString, "id", getPackageName());
            int imgResId = getResources().getIdentifier("img_" + slotIdString, "id", getPackageName());

            if (frameResId != 0 && imgResId != 0) {
                ImageView carImageView = findViewById(imgResId);

                if (slot.isOccupied()) {
                    carImageView.setVisibility(View.VISIBLE);
                } else {
                    carImageView.setVisibility(View.GONE);
                }
            }
        }
    }

    // ==========================================
    // 7. POPUPS
    // ==========================================

    // --- Intake: slot ocupado / confirmação ---

    private void showIntakeOccupiedPopup(String slotIdName) {
        tvVehicleInfo.setText("Slot: " + slotIdName + "\nO Slot está Ocupado!");
        txtMapQuestion.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setText("Back");

        cardPopup.setVisibility(View.VISIBLE);
    }

    private void showIntakeConfirmPopup(String slotIdName) {
        selectedSlotId = slotIdName;
        tvVehicleInfo.setText("Modelo: " + vehicleModel + "\nSlot: " + selectedSlotId);
        txtMapQuestion.setVisibility(View.VISIBLE);
        btnYes.setVisibility(View.VISIBLE);
        btnNo.setText("No");

        cardPopup.setVisibility(View.VISIBLE);
    }

    // --- Consulta: info do slot ---

    private void popUpInformationSlot(String clickedSlotId) {
        if (!isMapDataLoaded()) return;

        Slot selectedSlot = findSlotById(clickedSlotId);

        if (selectedSlot != null && selectedSlot.isOccupied()) {
            showOccupiedSlotOptions(clickedSlotId, selectedSlot.getCurrent_vin());
        } else {
            showEmptySlotPopup();
        }
    }

    private void showOccupiedSlotOptions(String slotId, String vin) {
        String mensagem = "Slot: " + slotId
                + "\nVehicle Identification Number: " + vin
                + "\nStatus: Occupied";

        txtVehicleDescription.setText(mensagem);
        txtVehicleVin.setText("VIN: " + vin);

        cardPopup.setVisibility(View.GONE);
        cardParkingOptions.setVisibility(View.VISIBLE);
    }

    private void showEmptySlotPopup() {
        tvVehicleInfo.setText("Este slot está vazio");
        txtMapQuestion.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setText("Back");

        cardParkingOptions.setVisibility(View.GONE);
        cardPopup.setVisibility(View.VISIBLE);
    }

    // --- Popup de peças ---

    private void popUpMeuParts(String clickedSlotId) {
        if (!isMapDataLoaded()) return;

        Slot selectedSlot = findSlotById(clickedSlotId);

        if (selectedSlot == null) {
            showToast("Slot not found.");
            return;
        }

        if (!selectedSlot.isOccupied()) {
            showToast("Este slot está livre.");
            return;
        }

        loadVehiclePartsInBackground(selectedSlot);
    }

    private void loadVehiclePartsInBackground(final Slot slot) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapController controller = new MapController();
                    Vehicle vehicle = controller.getVehicle(MapActivity.this, slot.getCurrent_vin());
                    final List<Parts> parts = vehicle.getPartsList();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showPartsPopup(parts);
                        }
                    });

                } catch (final Exception e) {
                    Log.e("MapActivity", "Error fetching vehicle", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void showPartsPopup(List<Parts> parts) {
        if (parts == null || parts.isEmpty()) {
            showToast("Este veículo não tem peças.");
            return;
        }

        List<String> nomes = new ArrayList<>();
        for (Parts p : parts) {
            nomes.add(p.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MapActivity.this,
                android.R.layout.simple_list_item_multiple_choice,
                nomes);

        listViewParts.setAdapter(adapter);

        cardParkingOptions.setVisibility(View.GONE);
        cardPartsPopup.setVisibility(View.VISIBLE);
    }

    // --- Diálogo: remover veículo sem peças ---

    private void showScrapConfirmDialog(final Slot slot, final String vin, final List<String> selectedNames) {
        new AlertDialog.Builder(MapActivity.this)
                .setTitle("Veículo sem peças")
                .setMessage("Este veículo vai ficar sem peças.\nQueres removê-lo deste slot?")
                .setCancelable(false)
                .setPositiveButton("Remover veículo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startMovingParts(slot, vin, selectedNames);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onScrapCancelled();
                    }
                })
                .show();
    }

    // --- Botão: voltar ao MainActivity (depois de registar o intake) ---

    private void showBackToMainButton() {
        if (btnBackToMain != null) {
            btnBackToMain.setVisibility(View.VISIBLE);
            return;
        }

        btnBackToMain = new Button(MapActivity.this);
        btnBackToMain.setText("Voltar ao menu");
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackToMain();
            }
        });

        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.setMargins(0, 0, 0, margin);

        // addContentView põe o botão por cima do layout, sem mexer no activity_map.xml
        addContentView(btnBackToMain, params);
    }

    private void goBackToMain() {
        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // ==========================================
    // 8. UTILITÁRIOS
    // ==========================================

    /** Procura um slot na lista carregada pelo ID (ex: "A3"). Devolve null se não existir. */
    private Slot findSlotById(String slotId) {
        if (globalSlotsList == null) return null;

        for (Slot slot : globalSlotsList) {
            if (slot.getSlotID().equals(slotId)) {
                return slot;
            }
        }
        return null;
    }

    /** Mostra "Loading map data..." e devolve false se a lista de slots ainda não carregou. */
    private boolean isMapDataLoaded() {
        if (globalSlotsList == null) {
            showToast("Loading map data...");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(MapActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToastLong(String message) {
        Toast.makeText(MapActivity.this, message, Toast.LENGTH_LONG).show();
    }
}