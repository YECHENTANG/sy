package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {
	private void RepeatedPositioning( List<Player> detectives){

		for(int j = 0; j < detectives.size() - 1 ; j++){
			if(detectives.get(j).location() == detectives.get(j+1).location())
			throw new IllegalArgumentException("some player on some location");
		}

	}
	private ImmutableList<Player> neweveryone( Player mrX ,List<Player> detectives){
		List<Player> everyone = new ArrayList<>();
		everyone.add(mrX);
		everyone.addAll(detectives);
		return ImmutableList.copyOf(everyone);

	}

	//--------------------------------------------------initialization
	final private class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private ImmutableList<Player> everyone;

//--------------------------------------------------constructor

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.everyone=neweveryone(mrX,detectives);

//--------------------------------------------------gametest
			if(setup.graph.hashCode() == 0)
				throw new IllegalArgumentException("Graph empty");
			if(remaining.isEmpty()) throw new IllegalArgumentException("No players");
			if ( setup .moves .isEmpty ( ) ) throw new  IllegalArgumentException ( "Moves is empty" )  ;
			if ( detectives .isEmpty ()) throw new  IllegalArgumentException  ( "Detectives is empty" );
			if (mrX.isDetective()){throw new  IllegalArgumentException(" mrx is empty");
			}
//-------------------------------------------------- detective
			for (Player p:detectives){
				if(!p.isDetective()){
					throw new IllegalArgumentException("detectives cant be mrx");}
				if (p.has(ScotlandYard.Ticket.DOUBLE))
					throw new IllegalArgumentException("detectives cant take double ticket");
				if (p.has(ScotlandYard.Ticket.SECRET))
					throw new IllegalArgumentException("detectives cant take secret ticket");
			}
//--------------------------------------------------loction
			RepeatedPositioning(detectives);
//--------------------------------------------------winner
			/*Set<Piece> win = new HashSet<>();
			if(mrxWins())
				win.add(mrX.piece());
			if(detectivesWin()){
				for(Player x : detectives)
					win.add(x.piece());
			}*/
//ImmutableSet.copyOf(win);
			this.winner = winerget();

		}

		//-------------------------------------------------- getter
		@Nonnull
		@Override
		public GameSetup getSetup() {
			return this.setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			Set<Piece> p = new HashSet<>();
			p.add(mrX.piece());
			for(Player d : this.detectives)
				p.add(d.piece());
			return ImmutableSet.copyOf(p);
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			for(Player p : this.detectives){
				if(p.piece() == detective){
					return Optional.of(p.location());}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for (Player p:everyone){
				if (p.piece()==piece){
					return Optional.of(new TicketBoard() {
						@Override
						public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
							return p.tickets().get(ticket);
						}
					});
				}
			}
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}
		//--------------------------------------------------makesinglemove and make doublemove for avalibalemove

private static ImmutableSet<Move.SingleMove> makeSingleMoves(
		GameSetup setup,
		List<Player> detectives,
		Player player,
		int source){
	final var singleMoves = new ArrayList<Move.SingleMove>();

	for(int destination : setup.graph.adjacentNodes(source)) {
		boolean locationEmpty = true;
		for(Player p : detectives) {
			if (p.location() == destination)
				locationEmpty = false;
		}
		if(locationEmpty){
			for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {

				if(player.isMrX() && player.has(ScotlandYard.Ticket.SECRET)){

					singleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));

				}

				if (player.has(t.requiredTicket())){

					singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));

				}


			}
		}
	}
	return ImmutableSet.copyOf(singleMoves);
}

		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player mrX,
				int source
		){
			final var doubleMoves = new ArrayList<Move.DoubleMove>();
			Set<Move.SingleMove> singleMoves = new HashSet<>(makeSingleMoves(setup, detectives, mrX, source));
			for(Move.SingleMove move1  : singleMoves){
				int destination1 = move1.destination;
				Player midle = mrX.use(move1.tickets());

				for(int destination2 : setup.graph.adjacentNodes(destination1)){
					boolean locationEmpty = true;
					for(Player p : detectives) {
						if (p.location() == destination2) {
							locationEmpty = false;
						}
					}

					if(locationEmpty){
						for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
							if (midle.has(t.requiredTicket())){

								doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1,t.requiredTicket(),destination2));

							}
							if(mrX.has(ScotlandYard.Ticket.SECRET)){

								doubleMoves.add(new Move.DoubleMove(mrX.piece(),source,move1.ticket,destination1, ScotlandYard.Ticket.SECRET,destination2));

							}

						}
					}

				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}
		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return this.winner;
		}
		private Player getPlayer(Piece piece){
			Player tmp=null;
			for(Player p : everyone)
				if(p.piece().equals(piece))
					tmp = p;
			return tmp;
		}


		@Nonnull
		@Override

		public ImmutableSet<Move> getAvailableMoves() {
			if(!getWinner().isEmpty())
				return ImmutableSet.copyOf(new HashSet<>());
//--------------------------------------------------------------
			Set<Move.SingleMove> singleMoves = new HashSet<>();

			for (Player player : everyone) {
				if (remaining.contains(player.piece())) {
					singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				}
			}

			Set<Move> moves = new HashSet<>(singleMoves);

			if(this.remaining.contains(mrX.piece()) && mrX.has(ScotlandYard.Ticket.DOUBLE)&&this.log.size() +2 <=setup.moves.size()){

				Set<Move.DoubleMove> doubleMoves = new HashSet<>(makeDoubleMoves(setup, detectives, mrX, mrX.location()));

				moves.addAll(doubleMoves);
			}
			this.moves = ImmutableSet.copyOf(moves);

			return this.moves;
		}



		private List<LogEntry> updateLog(Move move,int location){
			List<LogEntry> Log = new ArrayList<>(this.log);
			for(ScotlandYard.Ticket t : move.tickets()){
				int size = Log.size();
				if(t != ScotlandYard.Ticket.DOUBLE){
					if(setup.moves.get(size) ){

						Log.add(LogEntry.reveal(t,location));

					}
					else Log.add(LogEntry.hidden(t));}
			}
			return Log;
		}

		private void removePiece(Set<Piece> pieces, Piece piece){
			pieces.remove(piece);

		}


		private Set<Piece> createSet(List<Player> everyone){
			Set<Piece> pieces = new HashSet<>();
			for(Player x : everyone)
				pieces.add(x.piece());

			return pieces;
		}

		private int visitMe(Move move){

			return move.accept(new Move.Visitor<>(){

				@Override
				public Integer visit(Move.SingleMove singleMove) {
					return singleMove.destination;
				}

				@Override
				public Integer visit(Move.DoubleMove doubleMove) {
					return doubleMove.destination2;
				}
			});
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);



			List<Player> detectiveList = new ArrayList<>();
			Set<Piece> remain =new HashSet<>(this.remaining);
			List<LogEntry> LogEntry = new ArrayList<>(this.log);

			for(Player p : everyone){

				Player pp = p;
				if(p.piece() == move.commencedBy()){

					pp=pp.use(move.tickets());

					int location = visitMe(move);
					pp=pp.at(location);

					if(move.commencedBy().isMrX()){
						LogEntry = updateLog(move,location);
					}

				}
				detectiveList.add(pp);
			}



//检查然后初始化
			if(remain.size() == 1 ){
				if (remain.contains(this.mrX.piece())){
					remain = createSet(detectiveList);
					removePiece(remain,move.commencedBy());
				}
				else{
					remain = createSet(detectiveList);
					for(Player p : detectives)
						removePiece(remain,p.piece());
				}

			}
			else {removePiece(remain, move.commencedBy());}


			for(Player p : detectives){
				if(makeSingleMoves(setup,detectives,p,p.location()).isEmpty())
					removePiece(remain,p.piece());}



			Player mrx = detectiveList.get(0);

			if(move.commencedBy().isDetective()){

				for(ScotlandYard.Ticket t : move.tickets())
					mrx = mrx.give(t);

			}


			detectiveList.remove(0);
			return new MyGameState(setup,ImmutableSet.copyOf(remain),ImmutableList.copyOf(LogEntry),mrx,ImmutableList.copyOf(detectiveList));

		}

		private boolean detectivesWin(){
			boolean detectivesWin = false;
			for(Player p : detectives){
				if(p.location() == mrX.location())
					detectivesWin = true;}
			if(makeSingleMoves(setup,detectives,mrX, mrX.location()).isEmpty() && this.remaining.contains(mrX.piece())){
				detectivesWin = true;}
			return detectivesWin;

		}


		/*private boolean mrxWins(){
			boolean mrxWins = false;
			if(this.log.size() == setup.moves.size() && this.remaining.contains(mrX.piece())){
				mrxWins = true;}
			int size = 0;

			Iterator<Player> itp=detectives.iterator();
			while (itp.hasNext()){
				Player p=itp.next();
				if(makeSingleMoves(setup,detectives,p, p.location()).isEmpty())
					size ++;
			}
			//for(Player p : detectives){

			//}
			if(size == detectives.size()){
				mrxWins =  true;}

			return mrxWins;
		}
*/

		private ImmutableSet<Piece> winerget() {
			Set<Piece> win = new HashSet<>();
			int size = 0;
			Iterator<Player> itp = detectives.iterator();
			while (itp.hasNext()) {
				Player p = itp.next();
				if (makeSingleMoves(setup, detectives, p, p.location()).isEmpty())
					size++;
			}
			if (this.log.size() == setup.moves.size() && this.remaining.contains(mrX.piece())) {

				win.add(mrX.piece());
			} else if (size == detectives.size()) {
				win.add(mrX.piece());
			}
//-------------------------------------------------------
				if (detectivesWin()){
					for (Player player:detectives){
						win.add(player.piece());
					}
				}
			return ImmutableSet.copyOf(win);
		}

	}
//-------------------------------------
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		//throw new RuntimeException("Implement me!");
		MyGameState S=new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
		return S;

	}

}